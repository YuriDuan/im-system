<template>
  <div :class="['msg', { self: isSelf }]">
    <div class="bubble">
      <div v-if="!isSelf" class="msg-name">{{ sender }}</div>
      <div class="msg-text">{{ msg.content || "" }}</div>

      <!-- 图片预览 -->
      <div v-if="isImage" class="msg-image" @click="openLightbox">
        <img :src="msg.fileUrl" :alt="msg.fileName || '图片'" />
      </div>

      <!-- 其他文件链接 -->
      <a
        v-else-if="msg.fileUrl"
        class="file-link"
        :href="msg.fileUrl"
        target="_blank"
        rel="noopener noreferrer"
      >
        📎 {{ msg.fileName || "附件" }}
      </a>

      <div class="msg-time">{{ msg.timestamp || now }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { store } from "../store.js";

const props = defineProps({
  msg: { type: Object, required: true },
});

const now = new Date().toLocaleTimeString();

const sender = computed(() => props.msg.fromUser || props.msg.username || "");
const isSelf = computed(() => sender.value === store.currentUser?.username);

const imageExts = ["jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"];
const isImage = computed(() => {
  if (!props.msg.fileUrl) return false;
  const url = props.msg.fileUrl.toLowerCase();
  return imageExts.some((ext) => url.endsWith("." + ext) || url.includes("." + ext + "?"));
});

function openLightbox() {
  if (isImage.value) {
    store.lightboxUrl = props.msg.fileUrl;
  }
}
</script>
