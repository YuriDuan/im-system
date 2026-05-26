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
      style="display:none"
      @change="handleFileSend"
    />
  </form>
</template>

<script setup>
import { ref, watch } from "vue";
import { store, showToast } from "../store.js";
import { sendWsMessage } from "../websocket.js";
import { uploadFile } from "../api.js";

const props = defineProps({
  visible: { type: Boolean, default: false },
});

const emit = defineEmits(["sent"]);

const text = ref("");
const msgInput = ref(null);
const fileInput = ref(null);

watch(
  () => props.visible,
  (v) => {
    if (v) {
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

async function handleFileSend(e) {
  const file = e.target.files[0];
  if (!file) return;

  if (!store.ws || store.ws.readyState !== WebSocket.OPEN) {
    showToast("WebSocket 未连接");
    e.target.value = "";
    return;
  }
  if (
    (store.currentMode === "friend" && !store.selectedFriend) ||
    (store.currentMode === "group" && !store.selectedGroup)
  ) {
    showToast("请先选择会话");
    e.target.value = "";
    return;
  }

  try {
    const uploadData = await uploadFile(file);
    if (!uploadData.success) {
      showToast(uploadData.message || "文件上传失败");
      e.target.value = "";
      return;
    }

    const payload = {
      username: store.currentUser.username,
      content: `[文件] ${uploadData.fileName || file.name}`,
      messageType: "file",
      fileUrl: uploadData.fileUrl,
      fileName: uploadData.fileName || file.name,
    };

    if (store.currentMode === "friend") {
      payload.type = "privateChat";
      payload.toUser = store.selectedFriend.username;
    } else {
      payload.type = "groupChat";
      payload.groupId = store.selectedGroup.id;
    }

    sendWsMessage(payload);
    showToast("文件发送成功");
    emit("sent");
  } catch (err) {
    showToast(err.message);
  } finally {
    e.target.value = "";
  }
}

function handleSend() {
  const content = text.value.trim();
  if (!content) return;
  if (!store.ws || store.ws.readyState !== WebSocket.OPEN) {
    showToast("WebSocket 未连接");
    return;
  }

  const payload = {
    username: store.currentUser.username,
    content,
    messageType: "text",
  };

  if (store.currentMode === "friend" && store.selectedFriend) {
    payload.type = "privateChat";
    payload.toUser = store.selectedFriend.username;
  } else if (store.currentMode === "group" && store.selectedGroup) {
    payload.type = "groupChat";
    payload.groupId = store.selectedGroup.id;
  } else {
    showToast("请先选择会话");
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
