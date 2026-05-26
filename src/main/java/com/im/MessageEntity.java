package com.im;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息实体类 - 支持群聊、私聊、文件分享、加密
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender; // 发送者

    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupEntity group; // 所属群组（null表示私聊）

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver; // 接收者（私聊时使用）

    @Column(columnDefinition = "TEXT")
    private String content; // 消息内容

    @Column(name = "message_type")
    private String messageType; // text, image, file, system

    @Column(name = "file_url")
    private String fileUrl; // 文件URL

    @Column(name = "file_name")
    private String fileName; // 文件名

    @Column(name = "is_encrypted")
    private Boolean isEncrypted = false; // 是否加密

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public MessageEntity(UserEntity sender, GroupEntity group, String content, String messageType) {
        this.sender = sender;
        this.group = group;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = LocalDateTime.now();
    }

    public MessageEntity(UserEntity sender, UserEntity receiver, String content, String messageType) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = LocalDateTime.now();
    }
}
