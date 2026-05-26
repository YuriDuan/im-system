<template>
  <Teleport to="body">
    <div v-if="store.lightboxUrl" class="lightbox-overlay" @click="close">
      <div class="lightbox-content">
        <img :src="store.lightboxUrl" alt="预览图片" @click.stop />
        <button class="lightbox-close" @click="close">✕</button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { store } from "../store.js";

function close() {
  store.lightboxUrl = "";
}
</script>
