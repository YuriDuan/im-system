<template>
  <div class="messages" ref="msgBox">
    <div v-if="!messages.length" class="empty">暂无消息，先打个招呼吧。</div>
    <MessageBubble v-for="(msg, idx) in messages" :key="idx" :msg="msg" />
  </div>
</template>

<script setup>
import { computed, watch, ref, nextTick } from "vue";
import { store } from "../store.js";
import MessageBubble from "./MessageBubble.vue";

const props = defineProps({
  messages: { type: Array, default: () => [] },
});

const msgBox = ref(null);

watch(
  () => props.messages.length,
  async () => {
    await nextTick();
    if (msgBox.value) {
      msgBox.value.scrollTop = msgBox.value.scrollHeight;
    }
  },
);
</script>
