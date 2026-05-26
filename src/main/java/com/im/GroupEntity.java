package com.im;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 聊天群组实体类
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 群组名称

    @Column
    private String description; // 群组描述

    @Column(name = "group_icon")
    private String groupIcon; // 群组头像

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator; // 创建者

    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> members = new HashSet<>();

    @Column(name = "member_count")
    private Integer memberCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GroupEntity(String name, String description, UserEntity creator) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.members.add(creator);
        this.memberCount = 1;
    }
}
