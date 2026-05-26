package com.im;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 好友关系仓库
 */
@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, Long> {
    
    // 查询用户的所有好友
    List<FriendEntity> findByUserIdAndStatus(Long userId, Integer status);
    
    // 查询两个用户是否是好友
    Optional<FriendEntity> findByUserIdAndFriendIdAndStatus(Long userId, Long friendId, Integer status);
    
    // 删除好友关系
    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    // 查询等待接受的好友请求（别人发给我的）
    List<FriendEntity> findByFriendIdAndStatus(Long friendId, Integer status);

    // 查找任意状态的好友关系
    Optional<FriendEntity> findByUserIdAndFriendId(Long userId, Long friendId);
}
