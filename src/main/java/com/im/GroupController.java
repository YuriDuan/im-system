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

    /**
     * 创建群组
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request) {
        String groupName = (String) request.get("groupName");
        String description = (String) request.get("description");
        Long creatorId = ((Number) request.get("creatorId")).longValue();

        if (groupName == null || groupName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "群组名称不能为空");
                    }});
        }

        return ResponseEntity.ok(groupService.createGroup(groupName, description, creatorId));
    }

    /**
     * 添加成员
     */
    @PostMapping("/{groupId}/members/add")
    public ResponseEntity<?> addMember(@PathVariable Long groupId, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        return ResponseEntity.ok(groupService.addMember(groupId, userId));
    }

    /**
     * 移除成员
     */
    @PostMapping("/{groupId}/members/remove")
    public ResponseEntity<?> removeMember(@PathVariable Long groupId, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        return ResponseEntity.ok(groupService.removeMember(groupId, userId));
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
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        return ResponseEntity.ok(groupService.deleteGroup(groupId, userId));
    }
}
