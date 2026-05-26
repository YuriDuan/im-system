import { reactive } from "vue";

const stored = {
  token: localStorage.getItem("im_token") || "",
  userId: Number(localStorage.getItem("im_userId")) || null,
  username: localStorage.getItem("im_username") || "",
};

export const store = reactive({
  // 当前用户
  currentUser: stored.token ? { token: stored.token, userId: stored.userId, username: stored.username } : null,

  // WebSocket
  ws: null,
  socketState: "等待连接",
  isOnline: false,
  onlineCount: 0,

  // 模式: friend | group
  currentMode: "friend",

  // 选中的会话
  selectedFriend: null,
  selectedGroup: null,

  // 缓存
  friendCache: [],
  groupCache: [],
  pendingRequests: [],

  // 会话消息
  privateConversations: {},
  groupConversations: {},

  // Toast
  toastMessage: "",
  toastVisible: false,
  toastTimer: null,

  // 图片预览
  lightboxUrl: "",

  // 表情栏
  showEmoji: false,

  // 配置
  get apiBase() {
    return window.APP_CONFIG?.API_BASE || `${window.location.origin}/api`;
  },
  get wsBase() {
    return window.APP_CONFIG?.WS_BASE || `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/im`;
  },
});

export function showToast(msg) {
  store.toastMessage = msg;
  store.toastVisible = true;
  clearTimeout(store.toastTimer);
  store.toastTimer = setTimeout(() => {
    store.toastVisible = false;
  }, 2400);
}

export function setCurrentUser(user) {
  store.currentUser = user;
  if (user) {
    localStorage.setItem("im_token", user.token);
    localStorage.setItem("im_userId", String(user.userId));
    localStorage.setItem("im_username", user.username);
  } else {
    localStorage.removeItem("im_token");
    localStorage.removeItem("im_userId");
    localStorage.removeItem("im_username");
  }
}

export function clearSession() {
  store.currentUser = null;
  store.selectedFriend = null;
  store.selectedGroup = null;
  store.friendCache = [];
  store.groupCache = [];
  store.pendingRequests = [];
  store.privateConversations = {};
  store.groupConversations = {};
  store.showEmoji = false;
  store.lightboxUrl = "";
  if (store.ws) {
    store.ws.close();
    store.ws = null;
  }
  localStorage.removeItem("im_token");
  localStorage.removeItem("im_userId");
  localStorage.removeItem("im_username");
}
