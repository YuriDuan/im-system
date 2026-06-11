<template>
  <div class="contacts-page">
    <!-- 好友请求 -->
    <div v-if="store.pendingRequests.length" class="section">
      <div class="section-title">新朋友 ({{ store.pendingRequests.length }})</div>
      <div v-for="req in store.pendingRequests" :key="req.id" class="contact-item">
        <div class="contact-avatar">{{ (req.fromUsername || "?")[0] }}</div>
        <div class="contact-info">
          <div class="contact-name">{{ req.fromUsername }}</div>
          <div class="contact-sub">请求添加你为好友</div>
        </div>
        <div class="contact-actions">
          <button class="accept-btn" @click="handleAccept(req)">接受</button>
          <button class="reject-btn" @click="handleReject(req)">拒绝</button>
        </div>
      </div>
    </div>

    <!-- 好友列表 -->
    <div class="section">
      <div class="section-title">好友 ({{ store.friendCache.length }})</div>
      <div v-if="store.friendCache.length === 0" class="empty-hint">暂无好友，点击右上角 + 添加</div>
      <div v-for="f in store.friendCache" :key="f.id" class="contact-item" @click="openChat(f)">
        <div class="contact-avatar" :class="{ online: f.isOnline }">{{ (f.remark || f.username || "?")[0] }}</div>
        <div class="contact-info">
          <div class="contact-name">{{ f.remark || f.username }}</div>
          <div class="contact-sub">{{ f.isOnline ? '在线' : '离线' }}<span v-if="f.signature"> · {{ f.signature }}</span></div>
        </div>
        <button class="danger-btn small" @click.stop="handleRemove(f)">删除</button>
      </div>
    </div>

    <!-- 群聊列表 -->
    <div class="section">
      <div class="section-title">群聊 ({{ store.groupCache.length }})</div>
      <div v-if="store.groupCache.length === 0" class="empty-hint">暂无群聊</div>
      <div v-for="g in store.groupCache" :key="g.id" class="contact-item" @click="openGroupChat(g)">
        <div class="contact-avatar group-avatar">{{ (g.name || "群")[0] }}</div>
        <div class="contact-info">
          <div class="contact-name">{{ g.name }}</div>
          <div class="contact-sub">{{ g.memberCount || 0 }} 人</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { store, showToast } from "../store.js";
import { acceptFriendRequest, rejectFriendRequest, removeFriend, getFriendList, getPendingRequests, getGroupList } from "../api.js";

async function handleAccept(req) {
  try {
    await acceptFriendRequest(req.id);
    showToast("已添加好友");
    await getPendingRequests();
    await getFriendList();
  } catch (e) { showToast(e.message); }
}

async function handleReject(req) {
  try {
    await rejectFriendRequest(req.id);
    showToast("已拒绝");
    await getPendingRequests();
  } catch (e) { showToast(e.message); }
}

async function handleRemove(f) {
  if (!confirm(`确定删除好友 ${f.remark || f.username}？`)) return;
  try {
    await removeFriend(f.friendId || f.id);
    showToast("已删除");
    await getFriendList();
  } catch (e) { showToast(e.message); }
}

function openChat(f) {
  store.currentTab = "chat";
  store.currentChatFilter = "private";
  store.currentMode = "friend";
  store.selectedFriend = f;
  store.selectedGroup = null;
}

function openGroupChat(g) {
  store.currentTab = "chat";
  store.currentChatFilter = "group";
  store.currentMode = "group";
  store.selectedGroup = g;
  store.selectedFriend = null;
}
</script>
