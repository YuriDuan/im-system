<template>
  <aside class="sidebar">
    <!-- 个人信息 -->
    <div class="profile">
      <div>
        <div class="profile-name">{{ store.currentUser?.username || "未登录" }}</div>
        <div class="profile-meta">{{ store.socketState }}</div>
      </div>
      <div class="chip">{{ store.isOnline ? "在线" : "离线" }}</div>
    </div>

    <!-- 模式切换 -->
    <div class="segment">
      <button :class="{ active: store.currentMode === 'friend' }" @click="switchMode('friend')">好友</button>
      <button :class="{ active: store.currentMode === 'group' }" @click="switchMode('group')">群聊</button>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <button @click="refreshCurrentMode">刷新</button>
      <button class="danger" @click="handleLogout">退出登录</button>
    </div>

    <!-- 好友搜索 -->
    <div v-if="store.currentMode === 'friend'" class="search-box">
      <input v-model="searchKeyword" placeholder="搜索用户名添加好友" @keydown.enter="handleSearch" />
      <button @click="handleSearch">搜索</button>
    </div>

    <!-- 群操作 -->
    <template v-if="store.currentMode === 'group'">
      <div class="search-box">
        <input v-model="createGroupName" placeholder="新群名称" @keydown.enter="handleCreateGroup" />
        <button @click="handleCreateGroup">建群</button>
      </div>
      <div class="search-box">
        <input v-model="joinGroupId" placeholder="输入群 ID 加入" @keydown.enter="handleJoinGroup" />
        <button @click="handleJoinGroup">加入</button>
      </div>
    </template>

    <!-- 好友请求 -->
    <FriendRequests v-if="store.currentMode === 'friend'" @updated="refreshCurrentMode" />

    <!-- 搜索结果 -->
    <div class="section-title">{{ store.currentMode === 'friend' ? '搜索结果' : '群操作' }}</div>
    <div class="list search-results">
      <div v-if="!searchResults.length && store.currentMode === 'group'" class="empty">创建群或输入群 ID 加入。</div>
      <div v-if="!searchResults.length && store.currentMode === 'friend' && searched" class="empty">没有找到可添加的用户。</div>
      <div
        v-for="user in searchResults"
        :key="user.id"
        class="item search-item"
      >
        <span class="avatar">{{ (user.username || "?").slice(0, 1).toUpperCase() }}</span>
        <span class="meta">
          <span class="name">{{ user.username }}</span>
          <span class="sub">{{ user.email || "" }}</span>
        </span>
        <button class="mini" @click="handleAddFriend(user.id)">添加</button>
      </div>
    </div>

    <!-- 好友/群聊列表 -->
    <div class="section-title">{{ store.currentMode === 'friend' ? '我的好友' : '我的群聊' }}</div>
    <div class="list main-list">
      <div v-if="!mainItems.length" class="empty">
        {{ store.currentMode === 'friend' ? '暂无好友，先搜索并添加一个吧。' : '暂无群聊，先创建一个或加入一个吧。' }}
      </div>

      <button
        v-for="item in mainItems"
        :key="item.id"
        :class="['item', { active: isSelected(item) }]"
        @click="selectItem(item)"
      >
        <span class="avatar">{{ item.avatar || (item.username || item.name || "?").slice(0, 1).toUpperCase() }}</span>
        <span class="meta">
          <span class="name">{{ item.displayName || item.username || item.name }}</span>
          <span class="sub">{{ item.subtitle }}</span>
        </span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { store, clearSession, showToast } from "../store.js";
import {
  getFriendList, getGroupList, searchUsers,
  sendFriendRequest, createGroup, joinGroup, getPendingRequests,
} from "../api.js";
import FriendRequests from "./FriendRequests.vue";

const emit = defineEmits(["selectFriend", "selectGroup", "logout"]);

const searchKeyword = ref("");
const createGroupName = ref("");
const joinGroupId = ref("");
const searchResults = ref([]);
const searched = ref(false);

const mainItems = computed(() => {
  if (store.currentMode === "friend") {
    return store.friendCache.map((f) => ({
      ...f,
      displayName: f.remark || f.username,
      avatar: (f.remark || f.username || "?").slice(0, 1).toUpperCase(),
      subtitle: f.isOnline ? "在线" : "离线",
    }));
  }
  return store.groupCache.map((g) => ({
    ...g,
    avatar: "群",
    displayName: g.name,
    subtitle: `ID: ${g.id} · ${g.memberCount}人`,
  }));
});

function isSelected(item) {
  if (store.currentMode === "friend") {
    return store.selectedFriend?.id === item.id;
  }
  return store.selectedGroup?.id === item.id;
}

function switchMode(mode) {
  store.currentMode = mode;
  store.selectedFriend = null;
  store.selectedGroup = null;
  searchResults.value = [];
  searched.value = false;
  searchKeyword.value = "";
  refreshCurrentMode();
}

function selectItem(item) {
  if (store.currentMode === "friend") {
    store.selectedFriend = item;
    store.selectedGroup = null;
    emit("selectFriend", item);
  } else {
    store.selectedGroup = item;
    store.selectedFriend = null;
    emit("selectGroup", item);
  }
}

async function refreshCurrentMode() {
  if (!store.currentUser) return;
  if (store.currentMode === "friend") {
    await getFriendList();
    await getPendingRequests();
  } else {
    await getGroupList();
  }
}

async function handleSearch() {
  const keyword = searchKeyword.value.trim();
  if (!keyword) { showToast("请输入用户名"); return; }
  try {
    searchResults.value = await searchUsers(keyword);
    searched.value = true;
  } catch (e) {
    showToast(e.message);
  }
}

async function handleAddFriend(friendId) {
  try {
    const data = await sendFriendRequest(friendId);
    showToast(data.message || "好友请求已发送");
    searchResults.value = [];
    searchKeyword.value = "";
    searched.value = false;
  } catch (e) {
    showToast(e.message);
  }
}

async function handleCreateGroup() {
  const name = createGroupName.value.trim();
  if (!name) { showToast("请输入群名称"); return; }
  try {
    const data = await createGroup(name);
    showToast(data.message || "建群成功");
    if (data.success) {
      createGroupName.value = "";
      await getGroupList();
    }
  } catch (e) {
    showToast(e.message);
  }
}

async function handleJoinGroup() {
  const id = Number(joinGroupId.value.trim());
  if (!id || Number.isNaN(id)) { showToast("请输入有效群 ID"); return; }
  try {
    const data = await joinGroup(id);
    showToast(data.message || "加入成功");
    if (data.success) {
      joinGroupId.value = "";
      await getGroupList();
    }
  } catch (e) {
    showToast(e.message);
  }
}

function handleLogout() {
  clearSession();
  emit("logout");
}

onMounted(() => {
  if (store.currentUser) refreshCurrentMode();
});
</script>
