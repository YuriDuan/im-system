<template>
  <div class="tab-bar">
    <button :class="['tab-item', { active: store.currentTab === 'chat' }]" @click="store.currentTab = 'chat'">
      <span class="tab-icon">💬</span>
      <span class="tab-label">聊天</span>
      <span v-if="totalUnread" class="badge">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
    </button>
    <button :class="['tab-item', { active: store.currentTab === 'contacts' }]" @click="switchToContacts">
      <span class="tab-icon">👥</span>
      <span class="tab-label">联系人</span>
      <span v-if="store.pendingRequests.length" class="badge">{{ store.pendingRequests.length }}</span>
    </button>
    <button :class="['tab-item', { active: store.currentTab === 'me' }]" @click="switchToMe">
      <span class="tab-icon">👤</span>
      <span class="tab-label">我的</span>
    </button>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { store } from "../store.js";
import { getMyProfile } from "../api.js";

const totalUnread = computed(() => {
  let total = 0;
  const counts = store.unreadCounts;
  for (const key in counts) {
    total += counts[key] || 0;
  }
  return total;
});

function switchToContacts() {
  store.currentTab = "contacts";
}

function switchToMe() {
  store.currentTab = "me";
  getMyProfile();
}
</script>
