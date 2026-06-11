package com.im;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 群组API控制器
 */
@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {
    @Autowired
    private GroupService groupService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建群组
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody Map<String, Object> request) {
        Long creatorId = extractUserId(authHeader);
        if (creatorId == null) {
            return ResponseEntity.status(401).body(fail("Unauthorized"));
        }
        String groupName = (String) request.get("groupName");
        String description = (String) request.get("description");

        if (groupName == null || groupName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(fail("群组名称不能为空"));
        }

        return ResponseEntity.ok(groupService.createGroup(groupName, description, creatorId));
    }

    /**
     * 添加成员
     */
    @PostMapping("/{groupId}/members/add")
    public ResponseEntity<?> addMember(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long groupId,
                                       @RequestBody Map<String, Long> request) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("Unauthorized"));
        }
        Long targetUserId = request.get("userId");
        if (targetUserId == null) targetUserId = userId;
        return ResponseEntity.ok(groupService.addMember(groupId, targetUserId));
    }

    /**
     * 移除成员
     */
    @PostMapping("/{groupId}/members/remove")
    public ResponseEntity<?> removeMember(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Long groupId,
                                          @RequestBody Map<String, Long> request) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("Unauthorized"));
        }
        Long targetUserId = request.get("userId");
        if (targetUserId == null) targetUserId = userId;
        return ResponseEntity.ok(groupService.removeMember(groupId, targetUserId));
    }

    /**
     * 获取用户的所有群组
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserGroups(@PathVariable Long userId) {
        List<Map<String, Object>> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * 获取群组详情
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupDetail(@PathVariable Long groupId) {
        Map<String, Object> groupDetail = groupService.getGroupDetail(groupId);
        if (groupDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groupDetail);
    }

    /**
     * 删除群组
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable Long groupId) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("Unauthorized"));
        }
        return ResponseEntity.ok(groupService.deleteGroup(groupId, userId));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;
        return jwtUtil.getUserIdFromToken(token);
    }

    private Map<String, Object> fail(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", false);
        m.put("message", msg);
        return m;
    }
}
