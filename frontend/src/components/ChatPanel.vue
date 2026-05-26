<template>
  <main class="chat">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div>
        <div class="chat-title">{{ title }}</div>
        <div class="chat-subtitle">{{ subtitle }}</div>
      </div>
    </div>

    <!-- 消息列表 -->
    <MessageList :messages="currentMessages" />

    <!-- 表情栏 -->
    <EmojiPicker @pick="onEmojiPick" />

    <!-- 输入框 -->
    <MessageComposer
      ref="composer"
      :visible="hasSession"
      @sent="onMessageSent"
    />
  </main>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { store } from "../store.js";
import MessageList from "./MessageList.vue";
import EmojiPicker from "./EmojiPicker.vue";
import MessageComposer from "./MessageComposer.vue";

const composer = ref(null);

const hasSession = computed(() => {
  return !!(store.selectedFriend || store.selectedGroup);
});

const title = computed(() => {
  if (store.selectedFriend) return store.selectedFriend.remark || store.selectedFriend.username;
  if (store.selectedGroup) return store.selectedGroup.name;
  return "请选择会话开始聊天";
});

const subtitle = computed(() => {
  if (store.selectedFriend) return store.selectedFriend.isOnline ? "当前在线" : "当前离线";
  if (store.selectedGroup) return `群 ID: ${store.selectedGroup.id} · ${store.selectedGroup.memberCount} 人`;
  return "支持文本、文件和表情消息";
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

function onMessageSent() {
  // 消息发送后的处理
}
</script>
