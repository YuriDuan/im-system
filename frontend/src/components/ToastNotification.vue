<template>
  <div class="toast" :class="{ active: store.toastVisible }">{{ store.toastMessage }}</div>
</template>

<script setup>
import { store } from "../store.js";
</script>
