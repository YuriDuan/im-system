<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <div class="brand-mark"></div>
        <div>
          <h1>即时通讯系统</h1>
          <p>支持注册、登录、好友、群聊和文件传输</p>
        </div>
      </div>

      <div class="tabs">
        <button type="button" :class="['tab', { active: isLogin }]" @click="switchTab('login')">登录</button>
        <button type="button" :class="['tab', { active: !isLogin }]" @click="switchTab('register')">注册</button>
      </div>

      <form v-if="isLogin" class="form" @submit.prevent="handleLogin">
        <div class="field">
          <label for="loginUsername">用户名</label>
          <input id="loginUsername" v-model="loginForm.username" autocomplete="username" required />
        </div>
        <div class="field">
          <label for="loginPassword">密码</label>
          <input id="loginPassword" v-model="loginForm.password" type="password" autocomplete="current-password" required />
        </div>
        <button class="primary" type="submit">登录</button>
        <div class="error">{{ loginError }}</div>
        <div class="hint">如果您已有账户，请直接登录。登录后将建立 WebSocket 连接。</div>
      </form>

      <form v-else class="form" @submit.prevent="handleRegister">
        <div class="field">
          <label for="registerUsername">用户名</label>
          <input id="registerUsername" v-model="registerForm.username" autocomplete="username" required />
        </div>
        <div class="field">
          <label for="registerEmail">邮箱</label>
          <input id="registerEmail" v-model="registerForm.email" type="email" autocomplete="email" required />
        </div>
        <div class="field">
          <label for="registerPassword">密码</label>
          <input id="registerPassword" v-model="registerForm.password" type="password" autocomplete="new-password" required />
        </div>
        <div class="field">
          <label for="registerConfirmPassword">确认密码</label>
          <input id="registerConfirmPassword" v-model="registerForm.confirmPassword" type="password" autocomplete="new-password" required />
        </div>
        <button class="primary" type="submit">注册</button>
        <div class="error">{{ registerError }}</div>
        <div class="hint">用户名建议 2 到 20 个字符，密码至少 6 位，邮箱需要填写有效地址。</div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { loginApi, registerApi } from "../api.js";
import { connectSocket } from "../websocket.js";
import { setCurrentUser, showToast, store } from "../store.js";

const emit = defineEmits(["loggedIn"]);

const isLogin = ref(true);
const loginError = ref("");
const registerError = ref("");

const loginForm = ref({ username: "", password: "" });
const registerForm = ref({ username: "", email: "", password: "", confirmPassword: "" });

function switchTab(tab) {
  isLogin.value = tab === "login";
  loginError.value = "";
  registerError.value = "";
}

async function handleLogin() {
  loginError.value = "";
  const { username, password } = loginForm.value;
  if (!username || !password) {
    loginError.value = "请输入用户名和密码";
    return;
  }

  try {
    const data = await loginApi(username, password);
    if (!data.success) {
      loginError.value = data.message || "登录失败";
      return;
    }

    setCurrentUser({
      token: data.token,
      userId: data.userId,
      username: data.username || username,
    });
    connectSocket();
    emit("loggedIn");
  } catch (error) {
    loginError.value = error.message;
  }
}

async function handleRegister() {
  registerError.value = "";
  const { username, email, password, confirmPassword } = registerForm.value;
  if (username.length < 2 || password.length < 6 || !email.includes("@")) {
    registerError.value = "用户名至少 2 位，密码至少 6 位，邮箱必须有效";
    return;
  }
  if (password !== confirmPassword) {
    registerError.value = "两次输入的密码不一致";
    return;
  }

  try {
    const data = await registerApi(username, email, password, confirmPassword);
    if (!data.success) {
      registerError.value = data.message || "注册失败";
      return;
    }

    showToast("注册成功，请登录");
    switchTab("login");
    loginForm.value.username = username;
    loginForm.value.password = "";
  } catch (error) {
    registerError.value = error.message;
  }
}
</script>
