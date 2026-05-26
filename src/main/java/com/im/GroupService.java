package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> createGroup(String groupName, String description, Long creatorId) {
        UserEntity creator = userRepository.findById(creatorId).orElse(null);
        if (creator == null) {
            return fail("创建者不存在");
        }

        Optional<GroupEntity> existing = groupRepository.findByNameAndCreator(groupName, creator);
        if (existing.isPresent()) {
            return fail("群组名称已存在");
        }

        GroupEntity group = new GroupEntity(groupName, description, creator);
        GroupEntity saved = groupRepository.save(group);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "群组创建成功");
        result.put("groupId", saved.getId());
        result.put("groupName", saved.getName());
        return result;
    }

    public Map<String, Object> addMember(Long groupId, Long userId) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return fail("群组不存在");
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return fail("用户不存在");
        }

        if (group.getMembers().contains(user)) {
            return fail("用户已在群组中");
        }

        group.getMembers().add(user);
        group.setMemberCount(group.getMembers().size());
        groupRepository.save(group);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成员添加成功");
        result.put("memberCount", group.getMemberCount());
        return result;
    }

    public Map<String, Object> removeMember(Long groupId, Long userId) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return fail("群组不存在");
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return fail("用户不存在");
        }

        if (group.getCreator().getId().equals(userId)) {
            return fail("不能移除群组创建者");
        }

        group.getMembers().remove(user);
        group.setMemberCount(group.getMembers().size());
        groupRepository.save(group);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "成员移除成功");
        result.put("memberCount", group.getMemberCount());
        return result;
    }

    public List<Map<String, Object>> getUserGroups(Long userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        return groupRepository.findByMembersContains(user).stream()
                .map(this::groupToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getGroupDetail(Long groupId) {
        return groupRepository.findById(groupId).map(this::groupToMap).orElse(null);
    }

    public Map<String, Object> deleteGroup(Long groupId, Long userId) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return fail("群组不存在");
        }

        if (!group.getCreator().getId().equals(userId)) {
            return fail("只有群组创建者才能删除群组");
        }

        groupRepository.delete(group);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "群组已删除");
        return result;
    }

    private Map<String, Object> groupToMap(GroupEntity group) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", group.getId());
        map.put("name", group.getName());
        map.put("description", group.getDescription());
        map.put("memberCount", group.getMemberCount());
        map.put("creatorId", group.getCreator().getId());
        map.put("creatorName", group.getCreator().getUsername());
        map.put("members", group.getMembers().stream()
                .map(member -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", member.getId());
                    item.put("username", member.getUsername());
                    return item;
                })
                .collect(Collectors.toList()));
        return map;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
