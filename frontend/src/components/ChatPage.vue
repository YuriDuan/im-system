<template>
  <div class="chat-page">
    <!-- 会话列表模式 -->
    <template v-if="!hasSession">
      <div class="chat-subtabs">
        <button :class="['subtab', { active: store.currentChatFilter === 'group' }]" @click="store.currentChatFilter = 'group'">群聊</button>
        <button :class="['subtab', { active: store.currentChatFilter === 'private' }]" @click="store.currentChatFilter = 'private'">单聊</button>
      </div>
      <!-- 群聊列表 -->
      <div v-if="store.currentChatFilter === 'group'" class="conv-list">
        <div v-if="store.groupCache.length === 0" class="empty-hint">暂无群聊，点击右上角 + 创建</div>
        <div v-for="g in store.groupCache" :key="g.id" class="conv-item" @click="openGroupChat(g)">
          <div class="conv-avatar group-avatar">{{ (g.name || "群")[0] }}</div>
          <div class="conv-info">
            <div class="conv-name">{{ g.name }}</div>
            <div class="conv-sub">{{ g.memberCount || 0 }} 人</div>
          </div>
          <span v-if="store.unreadCounts['g_' + g.id]" class="unread-badge">{{ store.unreadCounts['g_' + g.id] }}</span>
        </div>
      </div>
      <!-- 单聊列表 -->
      <div v-else class="conv-list">
        <div v-if="store.friendCache.length === 0" class="empty-hint">暂无好友，去联系人页面添加吧</div>
        <div v-for="f in store.friendCache" :key="f.id" class="conv-item" @click="openPrivateChat(f)">
          <div class="conv-avatar" :class="{ online: f.isOnline }">{{ (f.remark || f.username || "?")[0] }}</div>
          <div class="conv-info">
            <div class="conv-name">{{ f.remark || f.username }}</div>
            <div class="conv-sub">{{ f.isOnline ? '在线' : '离线' }}</div>
          </div>
          <span v-if="store.unreadCounts['p_' + f.username]" class="unread-badge">{{ store.unreadCounts['p_' + f.username] }}</span>
        </div>
      </div>
    </template>

    <!-- 聊天详情模式 -->
    <template v-else>
      <div class="chat-detail">
        <div class="chat-header">
          <button class="back-btn" @click="closeChat">← 返回</button>
          <div>
            <div class="chat-title">{{ chatTitle }}</div>
            <div class="chat-subtitle">{{ chatSubtitle }}</div>
          </div>
          <div class="chat-actions" v-if="canCall">
            <button class="call-btn" @click="handleStartCall('voice')">语音</button>
            <button class="call-btn video" @click="handleStartCall('video')">视频</button>
          </div>
        </div>
        <MessageList :messages="currentMessages" />
        <EmojiPicker @pick="onEmojiPick" />
        <MessageComposer ref="composer" :visible="hasSession" @sent="onMessageSent" />
      </div>
    </template>
  </div>
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

const chatTitle = computed(() => {
  if (store.selectedFriend) return store.selectedFriend.remark || store.selectedFriend.username;
  if (store.selectedGroup) return store.selectedGroup.name;
  return "";
});

const chatSubtitle = computed(() => {
  if (store.selectedFriend) return store.selectedFriend.isOnline ? "当前在线" : "当前离线";
  if (store.selectedGroup) return `群 ID: ${store.selectedGroup.id} · ${store.selectedGroup.memberCount} 人`;
  return "";
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

function openGroupChat(g) {
  store.currentMode = "group";
  store.selectedGroup = g;
  store.selectedFriend = null;
  const key = "g_" + g.id;
  if (store.unreadCounts[key]) {
    store.unreadCounts[key] = 0;
  }
}

function openPrivateChat(f) {
  store.currentMode = "friend";
  store.selectedFriend = f;
  store.selectedGroup = null;
  const key = "p_" + f.username;
  if (store.unreadCounts[key]) {
    store.unreadCounts[key] = 0;
  }
}

function closeChat() {
  store.selectedFriend = null;
  store.selectedGroup = null;
}

function onEmojiPick(emoji) {
  composer.value?.appendEmoji(emoji);
}

function handleStartCall(kind) {
  startCall(kind);
}

function onMessageSent() {}
</script>
