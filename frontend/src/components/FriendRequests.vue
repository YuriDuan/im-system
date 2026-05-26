<template>
  <div v-if="requests.length">
    <div class="section-title">好友请求 ({{ requests.length }})</div>
    <div class="list">
      <div v-for="req in requests" :key="req.id" class="request-item">
        <span class="avatar">{{ (req.fromUsername || "?").slice(0, 1).toUpperCase() }}</span>
        <span class="meta">
          <span class="name">{{ req.fromUsername }}</span>
          <span class="sub">{{ req.message || "请求添加您为好友" }}</span>
        </span>
        <div class="request-actions">
          <button class="mini accept" @click="handleAccept(req)">接受</button>
          <button class="mini reject" @click="handleReject(req)">拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { store, showToast } from "../store.js";
import { acceptFriendRequest, rejectFriendRequest, getFriendList, getPendingRequests } from "../api.js";

const emit = defineEmits(["updated"]);

const requests = computed(() => store.pendingRequests);

async function handleAccept(req) {
  try {
    const data = await acceptFriendRequest(req.id);
    showToast(data.message || "已接受好友请求");
    await getPendingRequests();
    await getFriendList();
    emit("updated");
  } catch (e) {
    showToast(e.message);
  }
}

async function handleReject(req) {
  try {
    const data = await rejectFriendRequest(req.id);
    showToast(data.message || "已拒绝好友请求");
    await getPendingRequests();
    emit("updated");
  } catch (e) {
    showToast(e.message);
  }
}
</script>
