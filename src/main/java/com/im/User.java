package com.im;
/**
 * 用户实体类
 */
public class User {
    private String username;      // 用户名
    private String sessionId;     // 会话ID

    public User() {
    }

    public User(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
