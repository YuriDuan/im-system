import { store, showToast } from "./store.js";

async function request(path, options = {}, withAuth = true) {
  const headers = { ...(options.headers || {}) };
  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = headers["Content-Type"] || "application/json";
  }
  if (withAuth && store.currentUser) {
    headers.Authorization = `Bearer ${store.currentUser.token}`;
  }
  const response = await fetch(`${store.apiBase}${path}`, { ...options, headers });
  const text = await response.text();
  let data = {};
  if (text) {
    try { data = JSON.parse(text); } catch { data = { message: text }; }
  }
  if (!response.ok) {
    throw new Error(data.message || `请求失败: ${response.status}`);
  }
  return data;
}

export function requestForm(path, formData, method = "POST") {
  return request(path, { method, body: formData });
}

// 认证
export async function loginApi(username, password) {
  return request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  }, false);
}

export async function registerApi(username, email, password, confirmPassword) {
  return request("/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, email, password, confirmPassword }),
  }, false);
}

// 好友
export async function getFriendList() {
  const data = await request("/friends/list");
  store.friendCache = data.friends || [];
  return store.friendCache;
}

export async function searchUsers(keyword) {
  const data = await request(`/friends/search?username=${encodeURIComponent(keyword)}`);
  return data.users || [];
}

export async function sendFriendRequest(friendId) {
  return request("/friends/request", {
    method: "POST",
    body: JSON.stringify({ friendId }),
  });
}

export async function getPendingRequests() {
  const data = await request("/friends/requests");
  store.pendingRequests = data.requests || [];
  return store.pendingRequests;
}

export async function acceptFriendRequest(requestId) {
  return request("/friends/accept", {
    method: "POST",
    body: JSON.stringify({ requestId }),
  });
}

export async function rejectFriendRequest(requestId) {
  return request("/friends/reject", {
    method: "POST",
    body: JSON.stringify({ requestId }),
  });
}

export async function removeFriend(friendId) {
  return request("/friends/remove", {
    method: "POST",
    body: JSON.stringify({ friendId }),
  });
}

// 群聊
export async function getGroupList() {
  const data = await request(`/groups/user/${store.currentUser.userId}`);
  store.groupCache = Array.isArray(data) ? data : [];
  return store.groupCache;
}

export async function createGroup(groupName) {
  return request("/groups/create", {
    method: "POST",
    body: JSON.stringify({ groupName, description: "" }),
  });
}

export async function joinGroup(groupId) {
  return request(`/groups/${groupId}/members/add`, {
    method: "POST",
    body: JSON.stringify({ userId: store.currentUser.userId }),
  });
}

// 个人信息
export async function getMyProfile() {
  const data = await request("/user/profile");
  store.currentUserProfile = data;
  return data;
}

export async function updateProfile(updates) {
  return request("/user/profile", {
    method: "PUT",
    body: JSON.stringify(updates),
  });
}

export async function uploadAvatar(file) {
  const formData = new FormData();
  formData.append("file", file);
  return requestForm("/user/avatar", formData, "PUT");
}

// 文件
export async function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);
  return requestForm("/files/upload", formData);
}
