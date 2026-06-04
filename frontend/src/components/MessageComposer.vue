<template>
  <form class="composer" :class="{ active: visible }" @submit.prevent="handleSend">
    <button type="button" class="icon-btn" title="表情" @click="toggleEmoji">😊</button>
    <button type="button" class="icon-btn" title="文件" @click="openFilePicker">📎</button>
    <input
      ref="msgInput"
      v-model="text"
      type="text"
      placeholder="输入消息，回车发送"
      @keydown.enter.exact.prevent="handleSend"
    />
    <button class="send-btn" type="submit">发送</button>
    <input
      ref="fileInput"
      type="file"
      style="display: none"
      @change="handleFileSend"
    />
  </form>
</template>

<script setup>
import { ref, watch } from "vue";
import { uploadFile } from "../api.js";
import { showToast, store } from "../store.js";
import { sendWsMessage } from "../websocket.js";

const props = defineProps({
  visible: { type: Boolean, default: false },
});

const emit = defineEmits(["sent"]);

const text = ref("");
const msgInput = ref(null);
const fileInput = ref(null);

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      text.value = "";
      setTimeout(() => msgInput.value?.focus(), 50);
    }
  },
);

function toggleEmoji() {
  store.showEmoji = !store.showEmoji;
}

function openFilePicker() {
  fileInput.value?.click();
}

function buildTargetPayload(basePayload) {
  if (store.currentMode === "friend" && store.selectedFriend) {
    return {
      ...basePayload,
      type: "privateChat",
      toUser: store.selectedFriend.username,
    };
  }
  if (store.currentMode === "group" && store.selectedGroup) {
    return {
      ...basePayload,
      type: "groupChat",
      groupId: store.selectedGroup.id,
    };
  }
  showToast("请先选择会话");
  return null;
}

async function handleFileSend(event) {
  const file = event.target.files?.[0];
  if (!file) {
    return;
  }

  if (!store.ws || store.ws.readyState !== WebSocket.OPEN) {
    showToast("WebSocket 未连接");
    event.target.value = "";
    return;
  }

  if (!store.selectedFriend && !store.selectedGroup) {
    showToast("请先选择会话");
    event.target.value = "";
    return;
  }

  try {
    const uploadData = await uploadFile(file);
    if (!uploadData.success) {
      showToast(uploadData.message || "文件上传失败");
      event.target.value = "";
      return;
    }

    const messageType = file.type.startsWith("image/") ? "image" : "file";
    const payload = buildTargetPayload({
      username: store.currentUser.username,
      content: messageType === "image" ? "" : `[文件] ${uploadData.fileName || file.name}`,
      messageType,
      fileUrl: uploadData.fileUrl,
      fileName: uploadData.fileName || file.name,
    });

    if (!payload) {
      event.target.value = "";
      return;
    }

    sendWsMessage(payload);
    showToast("文件发送成功");
    emit("sent");
  } catch (error) {
    showToast(error.message);
  } finally {
    event.target.value = "";
  }
}

function handleSend() {
  const content = text.value.trim();
  if (!content) {
    return;
  }
  if (!store.ws || store.ws.readyState !== WebSocket.OPEN) {
    showToast("WebSocket 未连接");
    return;
  }

  const payload = buildTargetPayload({
    username: store.currentUser.username,
    content,
    messageType: "text",
  });

  if (!payload) {
    return;
  }

  sendWsMessage(payload);
  text.value = "";
  emit("sent");
}

function appendEmoji(emoji) {
  text.value += emoji;
  msgInput.value?.focus();
}

defineExpose({ appendEmoji });
</script>
