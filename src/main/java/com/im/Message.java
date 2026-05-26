package com.im;
import com.alibaba.fastjson.annotation.JSONField;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 消息实体类
 */
public class Message {
    private String fromUser;      // 发送人
    private String toUser;        // 接收人 (null表示群发)
    private String content;       // 消息内容
    private String type;          // 消息类型: chat(聊天), system(系统消息)
    private String timestamp;     // 时间戳

    public Message() {
    }

    public Message(String fromUser, String content, String type) {
        this.fromUser = fromUser;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getter and Setter
    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
