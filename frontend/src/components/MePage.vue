<template>
  <div class="me-page">
    <!-- 头像 -->
    <div class="me-avatar-wrap" @click="openAvatarPicker">
      <div class="me-avatar">
        <img v-if="avatarSrc" :src="avatarSrc" alt="头像" />
        <span v-else class="me-avatar-letter">{{ (store.currentUser?.username || "?")[0] }}</span>
      </div>
      <div class="me-camera">📷</div>
    </div>
    <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarUpload" />

    <!-- 用户名 -->
    <div class="me-name">{{ store.currentUser?.username }}</div>

    <!-- 微信号 -->
    <div v-if="!editingWechatId" class="me-wechat-id" @click="startEditWechatId">
      微信号：{{ profile?.wechatId || '加载中...' }}
      <span class="copy-hint">（点击编辑/复制）</span>
    </div>
    <div v-else class="me-signature-edit">
      <input v-model="wechatIdDraft" placeholder="设置微信号" maxlength="30" @keydown.enter="saveWechatId" />
      <button class="primary small" @click="saveWechatId">保存</button>
      <button class="cancel-btn small" @click="editingWechatId = false">取消</button>
    </div>

    <!-- 个性签名 -->
    <div v-if="!editingSignature" class="me-signature" :class="{ empty: !profile?.signature }" @click="startEditSignature">
      {{ profile?.signature || '点击设置个性签名' }}
    </div>
    <div v-else class="me-signature-edit">
      <input v-model="sigDraft" placeholder="个性签名" maxlength="200" @keydown.enter="saveSignature" />
      <button class="primary small" @click="saveSignature">保存</button>
      <button class="cancel-btn small" @click="editingSignature = false">取消</button>
    </div>

    <!-- 退出登录 -->
    <button class="me-logout" @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { store, showToast, clearSession } from "../store.js";
import { getMyProfile, updateProfile, uploadAvatar } from "../api.js";
import { hangupCall } from "../call.js";

const avatarInput = ref(null);
const editingSignature = ref(false);
const sigDraft = ref("");
const editingWechatId = ref(false);
const wechatIdDraft = ref("");

const profile = computed(() => store.currentUserProfile);

const avatarSrc = computed(() => {
  const url = profile.value?.avatarUrl;
  if (!url) return "";
  // 将相对路径转为绝对地址
  if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:")) return url;
  const apiBase = store.apiBase || "";
  const origin = apiBase.replace(/\/api\/?$/, "");
  return origin + (url.startsWith("/") ? "" : "/") + url;
});

onMounted(async () => {
  try {
    await getMyProfile();
  } catch (e) { /* ignore */ }
});

function openAvatarPicker() {
  avatarInput.value?.click();
}

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0];
  if (!file) return;
  try {
    const data = await uploadAvatar(file);
    if (data.success) {
      showToast("头像已更新");
      await getMyProfile();
    } else {
      showToast(data.message || "上传失败");
    }
  } catch (err) { showToast(err.message); }
  e.target.value = "";
}

function copyWechatId() {
  const wid = profile.value?.wechatId;
  if (!wid) return;
  navigator.clipboard?.writeText(wid).then(() => showToast("已复制")).catch(() => showToast(wid));
}

function startEditWechatId() {
  wechatIdDraft.value = profile.value?.wechatId || "";
  editingWechatId.value = true;
}

async function saveWechatId() {
  const wid = wechatIdDraft.value.trim();
  if (!wid) { showToast("微信号不能为空"); return; }
  try {
    await updateProfile({ wechatId: wid });
    showToast("微信号已更新");
    editingWechatId.value = false;
    await getMyProfile();
  } catch (e) { showToast(e.message); }
}

function startEditSignature() {
  sigDraft.value = profile.value?.signature || "";
  editingSignature.value = true;
}

async function saveSignature() {
  try {
    await updateProfile({ signature: sigDraft.value });
    showToast("签名已更新");
    editingSignature.value = false;
    await getMyProfile();
  } catch (e) { showToast(e.message); }
}

function handleLogout() {
  hangupCall();
  clearSession();
}
</script>
