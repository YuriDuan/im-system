import { handleCallSignal, registerCallSignalSender } from "./call.js";
import { store, showToast } from "./store.js";

export function connectSocket() {
  if (store.ws) {
    store.ws.close();
    store.ws = null;
  }

  const url = `${store.wsBase}?username=${encodeURIComponent(store.currentUser.username)}&token=${encodeURIComponent(store.currentUser.token)}`;
  store.ws = new WebSocket(url);

  store.ws.onopen = () => {
    store.socketState = "已连接";
    store.isOnline = true;
    store.ws.send(JSON.stringify({
      type: "login",
      username: store.currentUser.username,
      token: store.currentUser.token,
    }));
  };

  store.ws.onmessage = (event) => {
    let msg;
    try {
      msg = JSON.parse(event.data);
    } catch {
      return;
    }

    if (msg.type === "onlineUsers") {
      store.socketState = `已连接，在线 ${msg.count} 人`;
      store.onlineCount = msg.count;
      return;
    }
    if (msg.type === "privateChat") {
      receivePrivateMessage(msg);
      return;
    }
    if (msg.type === "groupChat" || msg.type === "chat") {
      receiveGroupMessage(msg);
      return;
    }
    if (msg.type === "friendRequest") {
      showToast(msg.message || "收到新的好友请求");
      return;
    }
    if (msg.type && msg.type.startsWith("call-")) {
      handleCallSignal(msg);
      return;
    }
    if (msg.type === "error") {
      showToast(msg.message || "消息处理失败");
    }
  };

  store.ws.onerror = () => {
    store.socketState = "连接异常";
  };

  store.ws.onclose = () => {
    store.socketState = "连接断开，正在重连...";
    store.isOnline = false;
    if (store.currentUser) {
      setTimeout(connectSocket, 2500);
    }
  };
}

function receivePrivateMessage(msg) {
  const peer = msg.fromUser === store.currentUser.username ? msg.toUser : msg.fromUser;
  if (!store.privateConversations[peer]) store.privateConversations[peer] = [];
  store.privateConversations[peer].push(msg);
  if (store.selectedFriend && store.selectedFriend.username === peer) {
    store.privateConversations = { ...store.privateConversations };
  }
}

function receiveGroupMessage(msg) {
  const groupId = String(msg.groupId || "");
  if (!groupId) return;
  if (!store.groupConversations[groupId]) store.groupConversations[groupId] = [];
  store.groupConversations[groupId].push(msg);
  if (store.selectedGroup && String(store.selectedGroup.id) === groupId) {
    store.groupConversations = { ...store.groupConversations };
  }
}

export function sendWsMessage(payload) {
  if (!store.ws || store.ws.readyState !== WebSocket.OPEN) {
    showToast("WebSocket 未连接");
    return false;
  }
  store.ws.send(JSON.stringify(payload));
  return true;
}

registerCallSignalSender(sendWsMessage);
