<template>
  <AuthPage v-if="!store.currentUser" @logged-in="onLoggedIn" />
  <div v-else class="app-page">
    <TopActionBar />
    <div class="main-content">
      <ChatPage v-if="store.currentTab === 'chat'" />
      <ContactsPage v-if="store.currentTab === 'contacts'" />
      <MePage v-if="store.currentTab === 'me'" />
    </div>
    <BottomTabBar />
  </div>
  <ToastNotification />
  <ImageLightbox />
  <CallPanel />
</template>

<script setup>
import { onMounted } from "vue";
import { connectSocket } from "./websocket.js";
import { setCurrentUser, store } from "./store.js";
import { getFriendList, getPendingRequests, getGroupList } from "./api.js";
import AuthPage from "./components/AuthPage.vue";
import BottomTabBar from "./components/BottomTabBar.vue";
import CallPanel from "./components/CallPanel.vue";
import ChatPage from "./components/ChatPage.vue";
import ContactsPage from "./components/ContactsPage.vue";
import ImageLightbox from "./components/ImageLightbox.vue";
import MePage from "./components/MePage.vue";
import TopActionBar from "./components/TopActionBar.vue";
import ToastNotification from "./components/ToastNotification.vue";

function onLoggedIn() {
  // 登录后刷新数据
  getFriendList().catch(() => {});
  getPendingRequests().catch(() => {});
  getGroupList().catch(() => {});
}

onMounted(() => {
  const token = localStorage.getItem("im_token");
  const userId = localStorage.getItem("im_userId");
  const username = localStorage.getItem("im_username");
  if (token && userId && username) {
    setCurrentUser({ token, userId: Number(userId), username });
    connectSocket();
    // 初始化加载数据
    getFriendList().catch(() => {});
    getPendingRequests().catch(() => {});
    getGroupList().catch(() => {});
  }
});
</script>
