<template>
  <div :class="['msg', { self: isSelf }]">
    <div class="bubble">
      <div v-if="!isSelf" class="msg-name">{{ sender }}</div>
      <div v-if="showText" class="msg-text">{{ msg.content || "" }}</div>

      <div v-if="isImage" class="msg-image" @click="openLightbox">
        <img :src="absoluteFileUrl" :alt="msg.fileName || '图片'" />
      </div>

      <a
        v-else-if="msg.fileUrl"
        class="file-link"
        :href="absoluteFileUrl"
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

// 将相对路径 fileUrl 转为绝对地址（解决前后端分离部署时图片/文件无法加载的问题）
const absoluteFileUrl = computed(() => {
  const url = props.msg.fileUrl;
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:") || url.startsWith("blob:")) {
    return url;
  }
  // apiBase 类似 "https://railway.app/api"，取 origin 部分拼接 fileUrl
  const apiBase = store.apiBase || "";
  const origin = apiBase.replace(/\/api\/?$/, "");
  return origin + (url.startsWith("/") ? "" : "/") + url;
});

const imageExts = ["jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"];
const isImage = computed(() => {
  if (!absoluteFileUrl.value) return false;
  const url = absoluteFileUrl.value.toLowerCase();
  return (
    url.startsWith("data:image/") ||
    url.startsWith("blob:") ||
    imageExts.some((ext) => url.endsWith("." + ext) || url.includes("." + ext + "?"))
  );
});

const showText = computed(() => {
  if (isImage.value && props.msg.messageType === "sticker") {
    return false;
  }
  return !!(props.msg.content && props.msg.content.trim());
});

function openLightbox() {
  if (isImage.value) {
    store.lightboxUrl = absoluteFileUrl.value;
  }
}
</script>
