<template>
  <teleport to="body">
    <div v-if="visible" class="call-overlay">
      <div class="call-card">
        <div class="call-head">
          <div>
            <div class="call-title">{{ callTitle }}</div>
            <div class="call-subtitle">{{ callSubtitle }}</div>
          </div>
          <div class="chip">{{ kindLabel }}</div>
        </div>

        <div class="call-stage">
          <template v-if="store.call.kind === 'video'">
            <div class="call-remote">
              <video ref="remoteVideo" autoplay playsinline />
              <div v-if="!store.call.remoteStream" class="call-placeholder">
                {{ statusHint }}
              </div>
            </div>
            <div class="call-local">
              <video ref="localVideo" autoplay playsinline muted />
            </div>
          </template>

          <template v-else>
            <div class="voice-stage">
              <div class="voice-orb"></div>
              <div class="voice-text">{{ statusHint }}</div>
            </div>
            <audio ref="remoteAudio" autoplay />
            <audio ref="localAudio" muted autoplay />
          </template>
        </div>

        <div class="call-actions">
          <button v-if="store.call.incoming" class="call-btn accept" @click="accept">接听</button>
          <button v-if="store.call.incoming" class="call-btn reject" @click="reject">拒绝</button>
          <button v-if="store.call.active || store.call.status === 'calling'" class="call-btn hangup" @click="hangup">挂断</button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { computed, nextTick, ref, watch } from "vue";
import { acceptIncomingCall, hangupCall, rejectIncomingCall } from "../call.js";
import { store } from "../store.js";

const remoteVideo = ref(null);
const localVideo = ref(null);
const remoteAudio = ref(null);
const localAudio = ref(null);

const visible = computed(() => store.call.incoming || store.call.active || store.call.status === "calling");

const callTitle = computed(() => {
  const name = store.call.peerDisplayName || store.call.peerUsername || "对方";
  if (store.call.incoming) return `来电：${name}`;
  if (store.call.status === "calling") return `正在呼叫 ${name}`;
  if (store.call.active) return `与 ${name} 通话中`;
  return "通话";
});

const callSubtitle = computed(() => {
  return store.call.kind === "video" ? "视频通话" : "语音通话";
});

const kindLabel = computed(() => (store.call.kind === "video" ? "视频" : "语音"));

const statusHint = computed(() => {
  if (store.call.incoming) return "等待你接听";
  if (store.call.status === "calling") return "正在等待对方接听";
  if (store.call.active) return "通话已连接";
  return "通话中";
});

function attachMedia(element, stream) {
  if (!element || !stream) {
    return;
  }
  if (element.srcObject !== stream) {
    element.srcObject = stream;
  }
  element.play?.().catch(() => {});
}

async function syncMedia() {
  await nextTick();
  if (store.call.kind === "video") {
    attachMedia(remoteVideo.value, store.call.remoteStream);
    attachMedia(localVideo.value, store.call.localStream);
  } else {
    attachMedia(remoteAudio.value, store.call.remoteStream);
    attachMedia(localAudio.value, store.call.localStream);
  }
}

watch(
  () => [store.call.kind, store.call.localStream, store.call.remoteStream, store.call.status, store.call.incoming, store.call.active],
  syncMedia,
  { immediate: true, deep: true },
);

function accept() {
  acceptIncomingCall();
}

function reject() {
  rejectIncomingCall();
}

function hangup() {
  hangupCall();
}
</script>
