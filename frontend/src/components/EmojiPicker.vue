<template>
  <div v-if="store.showEmoji" class="emoji-bar active">
    <button
      v-for="emoji in emojis"
      :key="emoji"
      class="emoji"
      @click="$emit('pick', emoji)"
    >
      {{ emoji }}
    </button>

    <button
      v-for="sticker in stickers"
      :key="sticker.id"
      class="sticker"
      :title="sticker.name"
      @click="sendSticker(sticker)"
    >
      <img :src="sticker.url" :alt="sticker.name" />
    </button>
  </div>
</template>

<script setup>
import { showToast, store } from "../store.js";
import { sendWsMessage } from "../websocket.js";

defineEmits(["pick"]);

const emojis = ["😀", "😂", "😍", "👍", "🎉", "❤️", "🔥", "🙏"];

function makeSticker(id, label, bg, fg) {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128">
      <defs>
        <linearGradient id="g-${id}" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${bg[0]}" />
          <stop offset="100%" stop-color="${bg[1]}" />
        </linearGradient>
      </defs>
      <rect x="8" y="8" width="112" height="112" rx="28" fill="url(#g-${id})" />
      <circle cx="64" cy="56" r="24" fill="rgba(255,255,255,0.85)" />
      <text x="64" y="103" text-anchor="middle" font-size="22" font-family="Arial, sans-serif" font-weight="700" fill="${fg}">${label}</text>
    </svg>
  `;
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

const stickers = [
  { id: "smile", name: "笑脸", url: makeSticker("smile", "笑", ["#3ccf84", "#157447"], "#ffffff") },
  { id: "love", name: "爱心", url: makeSticker("love", "爱", ["#ff8a65", "#ff4d6d"], "#ffffff") },
  { id: "cool", name: "酷", url: makeSticker("cool", "酷", ["#4f8cff", "#3257ff"], "#ffffff") },
  { id: "wow", name: "惊讶", url: makeSticker("wow", "哇", ["#f59e0b", "#ef4444"], "#ffffff") },
];

function sendSticker(sticker) {
  if (!store.ws || store.ws.readyState !== WebSocket.OPEN) {
    showToast("WebSocket 未连接");
    return;
  }
  if (store.currentMode === "friend" && !store.selectedFriend) {
    showToast("请先选择会话");
    return;
  }
  if (store.currentMode === "group" && !store.selectedGroup) {
    showToast("请先选择会话");
    return;
  }

  const payload = {
    username: store.currentUser.username,
    content: "",
    messageType: "sticker",
    fileUrl: sticker.url,
    fileName: sticker.name,
  };

  if (store.currentMode === "friend") {
    payload.type = "privateChat";
    payload.toUser = store.selectedFriend.username;
  } else {
    payload.type = "groupChat";
    payload.groupId = store.selectedGroup.id;
  }

  sendWsMessage(payload);
}
</script>
