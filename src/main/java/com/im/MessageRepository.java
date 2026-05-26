package com.im;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 消息Repository
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    // 查询群组消息
    List<MessageEntity> findByGroupOrderByCreatedAtDesc(GroupEntity group);
    
    Page<MessageEntity> findByGroupOrderByCreatedAtDesc(GroupEntity group, Pageable pageable);

    // 查询私聊消息
    List<MessageEntity> findBySenderAndReceiverOrReceiverAndSenderOrderByCreatedAtDesc(
            UserEntity sender1, UserEntity receiver1,
            UserEntity receiver2, UserEntity sender2);

    // 查询特定用户的消息
    List<MessageEntity> findBySenderOrReceiverOrderByCreatedAtDesc(UserEntity sender, UserEntity receiver);
}
