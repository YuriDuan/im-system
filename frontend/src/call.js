import { markRaw } from "vue";
import { showToast, store } from "./store.js";

let signalSender = null;
let peerConnection = null;
let localStream = null;
let pendingOffer = null;
let pendingCandidates = [];

const rtcConfig = {
  iceServers: [
    { urls: ["stun:stun.l.google.com:19302", "stun:stun1.l.google.com:19302"] },
    { urls: ["stun:stun.cloudflare.com:3478"] },
    { urls: ["stun:stun.qq.com:3478"] },
    { urls: ["stun:stun.miwifi.com:3478"] },
  ],
};

export function registerCallSignalSender(sender) {
  signalSender = sender;
}

function sendSignal(payload) {
  if (typeof signalSender === "function") {
    return signalSender(payload) !== false;
  }
  showToast("WebSocket 未连接");
  return false;
}

function resetPeerConnection() {
  if (peerConnection) {
    try {
      peerConnection.onicecandidate = null;
      peerConnection.ontrack = null;
      peerConnection.onconnectionstatechange = null;
      peerConnection.close();
    } catch {
      // ignore
    }
  }
  peerConnection = null;
}

function stopLocalStream() {
  if (localStream) {
    localStream.getTracks().forEach((track) => track.stop());
  }
  localStream = null;
  store.call.localStream = null;
}

function clearCallState() {
  store.call.active = false;
  store.call.incoming = false;
  store.call.kind = "";
  store.call.status = "idle";
  store.call.callId = "";
  store.call.peerUsername = "";
  store.call.peerDisplayName = "";
  store.call.remoteStream = null;
  store.call.error = "";
}

function ensureIdle() {
  return !(store.call.active || store.call.incoming || store.call.status === "calling");
}

function buildCallId() {
  return `call_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

function getMediaConstraints(kind) {
  return kind === "video"
    ? { audio: true, video: true }
    : { audio: true, video: false };
}

async function getLocalMedia(kind) {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    throw new Error("当前浏览器不支持通话功能");
  }

  stopLocalStream();
  const stream = await navigator.mediaDevices.getUserMedia(getMediaConstraints(kind));
  localStream = stream;
  store.call.localStream = markRaw(stream);
  return stream;
}

function createPeerConnection(kind) {
  resetPeerConnection();

  peerConnection = new RTCPeerConnection(rtcConfig);
  peerConnection.onicecandidate = (event) => {
    if (!event.candidate || !store.call.peerUsername || !store.call.callId) {
      return;
    }
    if (!sendSignal({
      type: "call-ice",
      fromUser: store.currentUser.username,
      toUser: store.call.peerUsername,
      callId: store.call.callId,
      kind,
      candidate: event.candidate,
    })) {
      return;
    }
  };
  peerConnection.ontrack = (event) => {
    if (event.streams && event.streams[0]) {
      store.call.remoteStream = markRaw(event.streams[0]);
    }
  };
  peerConnection.onconnectionstatechange = () => {
    if (peerConnection && ["failed", "disconnected", "closed"].includes(peerConnection.connectionState)) {
      if (store.call.active) {
        showToast("通话已断开");
      }
      finishCall(false);
    }
  };
  return peerConnection;
}

async function flushPendingCandidates() {
  if (!peerConnection || !pendingCandidates.length) {
    return;
  }
  for (const candidate of pendingCandidates) {
    try {
      await peerConnection.addIceCandidate(candidate);
    } catch {
      // ignore stale candidate
    }
  }
  pendingCandidates = [];
}

function setIncomingOffer(msg) {
  clearCallState();
  pendingOffer = msg;
  store.call.incoming = true;
  store.call.kind = msg.kind || "video";
  store.call.status = "incoming";
  store.call.callId = msg.callId || buildCallId();
  store.call.peerUsername = msg.fromUser || "";
  store.call.peerDisplayName = msg.fromUser || "";
  store.call.error = "";
  store.call.remoteStream = null;
  showToast(`${store.call.peerDisplayName} 发起了${store.call.kind === "video" ? "视频" : "语音"}通话`);
}

function isCurrentCall(msg) {
  if (!msg.callId || !store.call.callId) {
    return true;
  }
  return msg.callId === store.call.callId;
}

function finishCall(notifyPeer = true, reason = "hangup") {
  const peerUsername = store.call.peerUsername;
  const callId = store.call.callId;
  const kind = store.call.kind || "video";

  if (notifyPeer && peerUsername && callId) {
    if (!sendSignal({
      type: "call-hangup",
      fromUser: store.currentUser.username,
      toUser: peerUsername,
      callId,
      kind,
      reason,
    })) {
      // 如果信令发送失败，就直接结束本地状态
      // 避免挂断按钮留下悬空的通话界面
    }
  }

  resetPeerConnection();
  stopLocalStream();
  pendingOffer = null;
  pendingCandidates = [];
  clearCallState();
}

export async function startCall(kind) {
  if (!store.selectedFriend) {
    showToast("请先选择好友");
    return;
  }
  if (!ensureIdle()) {
    showToast("当前已有通话");
    return;
  }

  try {
    const stream = await getLocalMedia(kind);
    store.call.kind = kind;
    store.call.status = "calling";
    store.call.peerUsername = store.selectedFriend.username;
    store.call.peerDisplayName = store.selectedFriend.remark || store.selectedFriend.username;
    store.call.callId = buildCallId();
    store.call.remoteStream = null;

    const pc = createPeerConnection(kind);
    stream.getTracks().forEach((track) => pc.addTrack(track, stream));

    const offer = await pc.createOffer({
      offerToReceiveAudio: true,
      offerToReceiveVideo: kind === "video",
    });
    await pc.setLocalDescription(offer);

    if (!sendSignal({
      type: "call-offer",
      fromUser: store.currentUser.username,
      toUser: store.call.peerUsername,
      callId: store.call.callId,
      kind,
      sdp: offer,
    })) {
      finishCall(false, "error");
      return;
    }
  } catch (error) {
    finishCall(false, "error");
    showToast(error.message || "发起通话失败");
  }
}

export async function acceptIncomingCall() {
  if (!store.call.incoming || !pendingOffer) {
    return;
  }

  try {
    const kind = store.call.kind || pendingOffer.kind || "video";
    const offer = pendingOffer;
    const queuedCandidates = pendingCandidates.slice();

    const stream = await getLocalMedia(kind);
    const pc = createPeerConnection(kind);
    pendingCandidates = queuedCandidates;
    stream.getTracks().forEach((track) => pc.addTrack(track, stream));

    await pc.setRemoteDescription(new RTCSessionDescription(offer.sdp));
    await flushPendingCandidates();

    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);

    if (!sendSignal({
      type: "call-answer",
      fromUser: store.currentUser.username,
      toUser: store.call.peerUsername,
      callId: store.call.callId,
      kind,
      sdp: answer,
    })) {
      finishCall(false, "error");
      return;
    }

    store.call.active = true;
    store.call.incoming = false;
    store.call.status = "active";
    pendingOffer = null;
    showToast("通话已接通");
  } catch (error) {
    finishCall(false, "error");
    showToast(error.message || "接听失败");
  }
}

export function rejectIncomingCall() {
  if (!store.call.incoming) {
    return;
  }
  finishCall(true, "rejected");
}

export function hangupCall() {
  if (!store.call.active && !store.call.incoming && store.call.status !== "calling") {
    return;
  }
  finishCall(true, "hangup");
}

export function handleCallSignal(msg) {
  if (!msg || !msg.type || !msg.type.startsWith("call-")) {
    return;
  }
  if (!store.currentUser || msg.fromUser === store.currentUser.username) {
    return;
  }

  if (msg.type === "call-offer") {
    if (!ensureIdle()) {
      if (!sendSignal({
        type: "call-busy",
        fromUser: store.currentUser.username,
        toUser: msg.fromUser,
        callId: msg.callId,
        kind: msg.kind || "video",
      })) {
        return;
      }
      return;
    }
    pendingCandidates = [];
    setIncomingOffer(msg);
    return;
  }

  if (msg.type === "call-answer") {
    if (store.call.status !== "calling") {
      return;
    }
    if (!isCurrentCall(msg) || !peerConnection) {
      return;
    }

    peerConnection
      .setRemoteDescription(new RTCSessionDescription(msg.sdp))
      .then(() => flushPendingCandidates())
      .then(() => {
        store.call.active = true;
        store.call.status = "active";
        showToast("通话已接通");
      })
      .catch((error) => {
        showToast(error.message || "接通失败");
        finishCall(false, "error");
      });
    return;
  }

  if (msg.type === "call-ice") {
    if (!isCurrentCall(msg)) {
      return;
    }
    const candidate = msg.candidate;
    if (!candidate) {
      return;
    }
    if (peerConnection && peerConnection.remoteDescription) {
      peerConnection.addIceCandidate(candidate).catch(() => {});
    } else {
      pendingCandidates.push(candidate);
    }
    return;
  }

  if (msg.type === "call-hangup") {
    if (store.call.callId && msg.callId && msg.callId !== store.call.callId) {
      return;
    }
    finishCall(false, msg.reason || "hangup");
    return;
  }

  if (msg.type === "call-busy") {
    if (store.call.status === "calling" && isCurrentCall(msg)) {
      showToast("对方正在通话中");
      finishCall(false, "busy");
    }
  }
}
