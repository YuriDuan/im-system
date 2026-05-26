<template>
  <div class="emoji-bar" :class="{ active: store.showEmoji }">
    <button
      v-for="emoji in emojis"
      :key="emoji"
      class="emoji"
      @click="$emit('pick', emoji)"
    >{{ emoji }}</button>
  </div>
</template>

<script setup>
import { store } from "../store.js";

defineEmits(["pick"]);

const emojis = ["😀", "😂", "😍", "👍", "🎉", "❤️", "🔥", "🙏"];
</script>
