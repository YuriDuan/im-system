import { reactive } from "vue";

const stored = {
  token: localStorage.getItem("im_token") || "",
  userId: Number(localStorage.getItem("im_userId")) || null,
  username: localStorage.getItem("im_username") || "",
};

export const store = reactive({
  currentUser: stored.token ? { token: stored.token, userId: stored.userId, username: stored.username } : null,

  ws: null,
  socketState: "等待连接",
  isOnline: false,
  onlineCount: 0,

  currentMode: "friend",
  selectedFriend: null,
  selectedGroup: null,

  friendCache: [],
  groupCache: [],
  pendingRequests: [],

  privateConversations: {},
  groupConversations: {},

  toastMessage: "",
  toastVisible: false,
  toastTimer: null,

  lightboxUrl: "",
  showEmoji: false,
  call: {
    active: false,
    incoming: false,
    kind: "",
    status: "idle",
    callId: "",
    peerUsername: "",
    peerDisplayName: "",
    localStream: null,
    remoteStream: null,
    error: "",
  },

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
  store.call.active = false;
  store.call.incoming = false;
  store.call.kind = "";
  store.call.status = "idle";
  store.call.callId = "";
  store.call.peerUsername = "";
  store.call.peerDisplayName = "";
  store.call.localStream = null;
  store.call.remoteStream = null;
  store.call.error = "";
  if (store.ws) {
    store.ws.close();
    store.ws = null;
  }
  localStorage.removeItem("im_token");
  localStorage.removeItem("im_userId");
  localStorage.removeItem("im_username");
}
