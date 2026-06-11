<template>
  <div class="top-bar">
    <span class="top-title">{{ pageTitle }}</span>
    <div class="top-actions">
      <button class="action-btn" @click="showMenu = !showMenu">+</button>
      <div v-if="showMenu" class="action-menu">
        <button @click="handleAddFriend">添加好友</button>
        <button @click="handleCreateGroup">创建群聊</button>
      </div>
    </div>
    <!-- 添加好友弹窗 -->
    <div v-if="showAddFriend" class="modal-overlay" @click.self="showAddFriend = false">
      <div class="modal-card">
        <h3>添加好友</h3>
        <input v-model="searchKeyword" placeholder="输入用户名搜索" @keydown.enter="handleSearch" />
        <button class="primary small" @click="handleSearch">搜索</button>
        <div v-if="searchResults.length" class="search-results">
          <div v-for="u in searchResults" :key="u.id" class="search-row">
            <span class="sr-name">{{ u.username }}</span>
            <button class="primary small" @click="doAddFriend(u.id)">添加</button>
          </div>
        </div>
        <div v-if="searched && !searchResults.length" class="muted">未找到用户</div>
        <button class="modal-close" @click="showAddFriend = false">关闭</button>
      </div>
    </div>
    <!-- 创建群聊弹窗 -->
    <div v-if="showCreateGroup" class="modal-overlay" @click.self="showCreateGroup = false">
      <div class="modal-card">
        <h3>创建群聊</h3>
        <input v-model="groupName" placeholder="输入群聊名称" @keydown.enter="doCreateGroup" />
        <button class="primary small" @click="doCreateGroup">创建</button>
        <button class="modal-close" @click="showCreateGroup = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { store, showToast } from "../store.js";
import { searchUsers, sendFriendRequest, createGroup, getFriendList, getGroupList } from "../api.js";

const showMenu = ref(false);
const showAddFriend = ref(false);
const showCreateGroup = ref(false);
const searchKeyword = ref("");
const searchResults = ref([]);
const searched = ref(false);
const groupName = ref("");

const pageTitle = computed(() => {
  if (store.currentTab === "chat") return "聊天";
  if (store.currentTab === "contacts") return "联系人";
  return "我";
});

function handleAddFriend() {
  showMenu.value = false;
  showAddFriend.value = true;
  searchKeyword.value = "";
  searchResults.value = [];
  searched.value = false;
}

function handleCreateGroup() {
  showMenu.value = false;
  showCreateGroup.value = true;
  groupName.value = "";
}

async function handleSearch() {
  const kw = searchKeyword.value.trim();
  if (!kw) return;
  try {
    searchResults.value = await searchUsers(kw);
    searched.value = true;
  } catch (e) {
    showToast(e.message);
  }
}

async function doAddFriend(friendId) {
  try {
    await sendFriendRequest(friendId);
    showToast("好友请求已发送");
    showAddFriend.value = false;
  } catch (e) {
    showToast(e.message);
  }
}

async function doCreateGroup() {
  const name = groupName.value.trim();
  if (!name) { showToast("请输入群聊名称"); return; }
  try {
    await createGroup(name);
    showToast("群聊创建成功");
    showCreateGroup.value = false;
    await getGroupList();
  } catch (e) {
    showToast(e.message);
  }
}
</script>
