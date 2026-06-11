package com.im;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 支持注册、登录、密码加密
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // 加密后的密码

    @Column(unique = true)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl; // 头像URL

    @Column(unique = true, name = "wechat_id")
    private String wechatId; // 专属微信号

    @Column(name = "signature", length = 200)
    private String signature; // 个性签名

    @Column(name = "is_online")
    private Boolean isOnline = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserEntity(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
