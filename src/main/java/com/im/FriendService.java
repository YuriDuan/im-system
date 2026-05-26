package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FriendService {
    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_ACCEPTED = 1;
    private static final int STATUS_REJECTED = 2;

    /** 获取已接受的好友列表 */
    public List<Map<String, Object>> getFriendList(Long userId) {
        List<FriendEntity> friends = friendRepository.findByUserIdAndStatus(userId, STATUS_ACCEPTED);
        List<Map<String, Object>> result = new ArrayList<>();
        for (FriendEntity friend : friends) {
            userRepository.findById(friend.getFriendId()).ifPresent(user -> result.add(toUserMap(user, friend.getRemark())));
        }
        return result;
    }

    /** 发送好友请求 */
    public Map<String, Object> sendFriendRequest(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return fail("用户信息不完整");
        }
        if (userId.equals(friendId)) {
            return fail("不能添加自己为好友");
        }

        Optional<UserEntity> friendOpt = userRepository.findById(friendId);
        if (!friendOpt.isPresent()) {
            return fail("用户不存在");
        }

        // 检查是否已经是好友
        Optional<FriendEntity> existFriend = friendRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, STATUS_ACCEPTED);
        if (existFriend.isPresent()) {
            return fail("已经是好友了");
        }

        // 检查是否已经发送过等待中的请求
        Optional<FriendEntity> existPending = friendRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, STATUS_PENDING);
        if (existPending.isPresent()) {
            return fail("已发送过好友请求，请等待对方确认");
        }

        // 检查对方是否已向我发送请求（如果对方已发送，则直接成为好友）
        Optional<FriendEntity> reversePending = friendRepository.findByUserIdAndFriendIdAndStatus(friendId, userId, STATUS_PENDING);
        if (reversePending.isPresent()) {
            // 对方已经向我发送过请求，直接接受
            reversePending.get().setStatus(STATUS_ACCEPTED);
            friendRepository.save(reversePending.get());
            FriendEntity reciprocal = new FriendEntity(userId, friendId);
            reciprocal.setStatus(STATUS_ACCEPTED);
            friendRepository.save(reciprocal);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "你们已互为好友！");
            result.put("friend", toUserMap(friendOpt.get(), null));
            return result;
        }

        // 创建新的好友请求
        FriendEntity request = new FriendEntity(userId, friendId);
        request.setStatus(STATUS_PENDING);
        friendRepository.save(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "好友请求已发送");
        return result;
    }

    /** 获取等待我处理的好友请求 */
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        List<FriendEntity> requests = friendRepository.findByFriendIdAndStatus(userId, STATUS_PENDING);
        List<Map<String, Object>> result = new ArrayList<>();
        for (FriendEntity req : requests) {
            userRepository.findById(req.getUserId()).ifPresent(fromUser -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", req.getId());
                map.put("fromUserId", fromUser.getId());
                map.put("fromUsername", fromUser.getUsername());
                map.put("message", fromUser.getUsername() + " 请求添加您为好友");
                map.put("createdAt", req.getCreatedAt() != null ? req.getCreatedAt().toString() : "");
                result.add(map);
            });
        }
        return result;
    }

    /** 接受好友请求 */
    public Map<String, Object> acceptFriendRequest(Long requestId, Long currentUserId) {
        Optional<FriendEntity> opt = friendRepository.findById(requestId);
        if (!opt.isPresent()) {
            return fail("请求不存在");
        }

        FriendEntity request = opt.get();
        if (!request.getFriendId().equals(currentUserId)) {
            return fail("无权处理此请求");
        }
        if (request.getStatus() != STATUS_PENDING) {
            return fail("请求已处理");
        }

        // 更新请求状态为已接受
        request.setStatus(STATUS_ACCEPTED);
        friendRepository.save(request);

        // 创建双向好友关系（对方确认记录）
        Optional<FriendEntity> existReciprocal = friendRepository.findByUserIdAndFriendId(currentUserId, request.getUserId());
        if (!existReciprocal.isPresent()) {
            FriendEntity reciprocal = new FriendEntity(currentUserId, request.getUserId());
            reciprocal.setStatus(STATUS_ACCEPTED);
            friendRepository.save(reciprocal);
        } else {
            existReciprocal.get().setStatus(STATUS_ACCEPTED);
            friendRepository.save(existReciprocal.get());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已接受好友请求");
        return result;
    }

    /** 拒绝好友请求 */
    public Map<String, Object> rejectFriendRequest(Long requestId, Long currentUserId) {
        Optional<FriendEntity> opt = friendRepository.findById(requestId);
        if (!opt.isPresent()) {
            return fail("请求不存在");
        }

        FriendEntity request = opt.get();
        if (!request.getFriendId().equals(currentUserId)) {
            return fail("无权处理此请求");
        }
        if (request.getStatus() != STATUS_PENDING) {
            return fail("请求已处理");
        }

        request.setStatus(STATUS_REJECTED);
        friendRepository.save(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已拒绝好友请求");
        return result;
    }

    /** 添加好友（双向确认后直接添加，兼容旧接口） */
    public Map<String, Object> addFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return fail("用户信息不完整");
        }
        if (userId.equals(friendId)) {
            return fail("不能添加自己为好友");
        }

        Optional<UserEntity> friendOpt = userRepository.findById(friendId);
        if (!friendOpt.isPresent()) {
            return fail("用户不存在");
        }

        Optional<FriendEntity> existFriend = friendRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, STATUS_ACCEPTED);
        if (existFriend.isPresent()) {
            return fail("已经是好友了");
        }

        FriendEntity friend1 = new FriendEntity(userId, friendId);
        friend1.setStatus(STATUS_ACCEPTED);
        FriendEntity friend2 = new FriendEntity(friendId, userId);
        friend2.setStatus(STATUS_ACCEPTED);
        friendRepository.save(friend1);
        friendRepository.save(friend2);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "好友添加成功");
        result.put("friend", toUserMap(friendOpt.get(), null));
        return result;
    }

    public void removeFriend(Long userId, Long friendId) {
        friendRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendRepository.deleteByUserIdAndFriendId(friendId, userId);
    }

    public boolean isFriend(Long userId, Long friendId) {
        return friendRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, STATUS_ACCEPTED).isPresent();
    }

    public void setFriendRemark(Long userId, Long friendId, String remark) {
        friendRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, STATUS_ACCEPTED).ifPresent(friend -> {
            friend.setRemark(remark);
            friendRepository.save(friend);
        });
    }

    public List<Map<String, Object>> searchUsers(String username, Long currentUserId) {
        List<UserEntity> users = userRepository.findByUsernameContaining(username);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserEntity user : users) {
            if (user.getId().equals(currentUserId) || isFriend(currentUserId, user.getId())) {
                continue;
            }
            result.add(toUserMap(user, null));
        }
        return result;
    }

    private Map<String, Object> toUserMap(UserEntity user, String remark) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("avatarUrl", user.getAvatarUrl());
        map.put("isOnline", user.getIsOnline());
        map.put("remark", remark);
        return map;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
