package com.im;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class IMHandler extends TextWebSocketHandler {
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<String, WebSocketSession> usernameSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        broadcastOnlineUsers();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JSONObject jsonMsg = JSON.parseObject(message.getPayload());
            String type = jsonMsg.getString("type");

            if ("login".equals(type)) {
                String username = jsonMsg.getString("username");
                if (username != null && !username.trim().isEmpty()) {
                    usernameSessionMap.put(username, session);
                    session.getAttributes().put("username", username);
                    broadcastOnlineUsers();
                }
                return;
            }

            if ("privateChat".equals(type)) {
                sendPrivateMessage(session, jsonMsg);
                return;
            }

            if ("groupChat".equals(type) || "chat".equals(type)) {
                broadcastChatMessage(session, jsonMsg);
            }
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("type", "error");
            error.put("message", "消息处理失败: " + e.getMessage());
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(error.toJSONString()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            usernameSessionMap.remove(username);
        }
        sessions.remove(session);
        broadcastOnlineUsers();
    }

    private void sendPrivateMessage(WebSocketSession senderSession, JSONObject jsonMsg) throws Exception {
        String fromUser = getUsername(senderSession, jsonMsg);
        String toUser = jsonMsg.getString("toUser");
        String content = jsonMsg.getString("content");
        String fileUrl = jsonMsg.getString("fileUrl");
        String fileName = jsonMsg.getString("fileName");
        String messageType = jsonMsg.getString("messageType");

        if (fromUser == null || toUser == null) {
            return;
        }
        if ((content == null || content.trim().isEmpty()) && (fileUrl == null || fileUrl.trim().isEmpty())) {
            return;
        }

        JSONObject privateMsg = new JSONObject();
        privateMsg.put("type", "privateChat");
        privateMsg.put("username", fromUser);
        privateMsg.put("fromUser", fromUser);
        privateMsg.put("toUser", toUser);
        privateMsg.put("content", content == null ? "" : content);
        privateMsg.put("messageType", messageType == null ? "text" : messageType);
        privateMsg.put("fileUrl", fileUrl);
        privateMsg.put("fileName", fileName);
        privateMsg.put("timestamp", now());

        WebSocketSession targetSession = usernameSessionMap.get(toUser);
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(privateMsg.toJSONString()));
        }
        if (senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(privateMsg.toJSONString()));
        }
    }

    private void broadcastChatMessage(WebSocketSession session, JSONObject jsonMsg) throws Exception {
        String username = getUsername(session, jsonMsg);
        String content = jsonMsg.getString("content");
        String fileUrl = jsonMsg.getString("fileUrl");
        String fileName = jsonMsg.getString("fileName");
        String messageType = jsonMsg.getString("messageType");
        Object groupId = jsonMsg.get("groupId");

        if (username == null || groupId == null) {
            return;
        }
        if ((content == null || content.trim().isEmpty()) && (fileUrl == null || fileUrl.trim().isEmpty())) {
            return;
        }

        JSONObject chatMsg = new JSONObject();
        chatMsg.put("type", "groupChat");
        chatMsg.put("username", username);
        chatMsg.put("fromUser", username);
        chatMsg.put("content", content == null ? "" : content);
        chatMsg.put("groupId", groupId);
        chatMsg.put("messageType", messageType == null ? "text" : messageType);
        chatMsg.put("fileUrl", fileUrl);
        chatMsg.put("fileName", fileName);
        chatMsg.put("timestamp", now());
        broadcastMessage(chatMsg.toJSONString());
    }

    private String getUsername(WebSocketSession session, JSONObject jsonMsg) {
        String username = (String) session.getAttributes().get("username");
        if (username == null) {
            username = jsonMsg.getString("username");
        }
        return username;
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void broadcastMessage(String messageJson) throws Exception {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    private void broadcastOnlineUsers() throws Exception {
        JSONObject usersMsg = new JSONObject();
        usersMsg.put("type", "onlineUsers");
        usersMsg.put("users", usernameSessionMap.keySet());
        usersMsg.put("count", usernameSessionMap.size());
        broadcastMessage(usersMsg.toJSONString());
    }
}
