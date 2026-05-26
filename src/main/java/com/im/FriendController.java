package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {
    @Autowired
    private FriendService friendService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public ResponseEntity<?> getFriendList(@RequestHeader("Authorization") String token) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        List<Map<String, Object>> friends = friendService.getFriendList(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("friends", friends);
        return ResponseEntity.ok(result);
    }

    /** 发送好友请求 */
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        Object friendIdValue = request.get("friendId");
        if (!(friendIdValue instanceof Number)) {
            return ResponseEntity.badRequest().body(fail("friendId 不能为空"));
        }
        Long friendId = ((Number) friendIdValue).longValue();
        return ResponseEntity.ok(friendService.sendFriendRequest(userId, friendId));
    }

    /** 获取待处理的好友请求 */
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests(@RequestHeader("Authorization") String token) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        List<Map<String, Object>> requests = friendService.getPendingRequests(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("requests", requests);
        return ResponseEntity.ok(result);
    }

    /** 接受好友请求 */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        Object requestIdValue = request.get("requestId");
        if (!(requestIdValue instanceof Number)) {
            return ResponseEntity.badRequest().body(fail("requestId 不能为空"));
        }
        Long requestId = ((Number) requestIdValue).longValue();
        return ResponseEntity.ok(friendService.acceptFriendRequest(requestId, userId));
    }

    /** 拒绝好友请求 */
    @PostMapping("/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        Object requestIdValue = request.get("requestId");
        if (!(requestIdValue instanceof Number)) {
            return ResponseEntity.badRequest().body(fail("requestId 不能为空"));
        }
        Long requestId = ((Number) requestIdValue).longValue();
        return ResponseEntity.ok(friendService.rejectFriendRequest(requestId, userId));
    }

    /** 直接添加好友（兼容旧接口） */
    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        Object friendIdValue = request.get("friendId");
        if (!(friendIdValue instanceof Number)) {
            return ResponseEntity.badRequest().body(fail("friendId 不能为空"));
        }
        Long friendId = ((Number) friendIdValue).longValue();
        return ResponseEntity.ok(friendService.addFriend(userId, friendId));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFriend(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        Object friendIdValue = request.get("friendId");
        if (!(friendIdValue instanceof Number)) {
            return ResponseEntity.badRequest().body(fail("friendId 不能为空"));
        }
        friendService.removeFriend(userId, ((Number) friendIdValue).longValue());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "好友已删除");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String username, @RequestHeader("Authorization") String token) {
        Long currentUserId = userIdFromToken(token);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        List<Map<String, Object>> users = friendService.searchUsers(username, currentUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("users", users);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check/{userId}/{friendId}")
    public ResponseEntity<?> isFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        Map<String, Object> result = new HashMap<>();
        result.put("isFriend", friendService.isFriend(userId, friendId));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/remark")
    public ResponseEntity<?> setFriendRemark(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        Long userId = userIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("登录已失效，请重新登录"));
        }
        Object friendIdValue = request.get("friendId");
        if (!(friendIdValue instanceof Number)) {
            return ResponseEntity.badRequest().body(fail("friendId 不能为空"));
        }
        String remark = (String) request.get("remark");
        friendService.setFriendRemark(userId, ((Number) friendIdValue).longValue(), remark);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "备注已更新");
        return ResponseEntity.ok(result);
    }

    private Long userIdFromToken(String token) {
        if (token == null) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
