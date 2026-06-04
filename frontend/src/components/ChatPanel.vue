<template>
  <main class="chat">
    <div class="chat-header">
      <div>
        <div class="chat-title">{{ title }}</div>
        <div class="chat-subtitle">{{ subtitle }}</div>
      </div>
      <div class="chat-actions" v-if="canCall">
        <button class="call-btn" @click="handleStartCall('voice')">语音通话</button>
        <button class="call-btn video" @click="handleStartCall('video')">视频通话</button>
      </div>
    </div>

    <MessageList :messages="currentMessages" />
    <EmojiPicker @pick="onEmojiPick" />
    <MessageComposer
      ref="composer"
      :visible="hasSession"
      @sent="onMessageSent"
    />
  </main>
</template>

<script setup>
import { computed, ref } from "vue";
import { startCall } from "../call.js";
import { store } from "../store.js";
import EmojiPicker from "./EmojiPicker.vue";
import MessageComposer from "./MessageComposer.vue";
import MessageList from "./MessageList.vue";

const composer = ref(null);

const hasSession = computed(() => !!(store.selectedFriend || store.selectedGroup));
const canCall = computed(() => !!store.selectedFriend && store.currentMode === "friend");

const title = computed(() => {
  if (store.selectedFriend) return store.selectedFriend.remark || store.selectedFriend.username;
  if (store.selectedGroup) return store.selectedGroup.name;
  return "请选择会话开始聊天";
});

const subtitle = computed(() => {
  if (store.selectedFriend) return store.selectedFriend.isOnline ? "当前在线" : "当前离线";
  if (store.selectedGroup) return `群 ID: ${store.selectedGroup.id} · ${store.selectedGroup.memberCount} 人`;
  return "支持文本、文件、表情、图片表情包以及语音/视频通话";
});

const currentMessages = computed(() => {
  if (store.selectedFriend && store.currentMode === "friend") {
    return store.privateConversations[store.selectedFriend.username] || [];
  }
  if (store.selectedGroup && store.currentMode === "group") {
    return store.groupConversations[String(store.selectedGroup.id)] || [];
  }
  return [];
});

function onEmojiPick(emoji) {
  composer.value?.appendEmoji(emoji);
}

function handleStartCall(kind) {
  startCall(kind);
}

function onMessageSent() {
  // keep layout stable after sending
}
</script>
