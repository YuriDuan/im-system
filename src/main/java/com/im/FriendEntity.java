package com.im;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "friends")
public class FriendEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // 用户ID

    @Column(nullable = false)
    private Long friendId; // 好友ID

    @Column(name = "remark")
    private String remark; // 好友备注名

    @Column(name = "status")
    private Integer status = 0; // 状态：0-等待接受，1-已接受，2-已拒绝

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public FriendEntity(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
        this.createdAt = LocalDateTime.now();
    }
}
