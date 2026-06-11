package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户个人信息 API — 微信号、头像、签名
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户完整个人信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("Unauthorized"));
        }
        UserEntity user = userAuthService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(fail("User not found"));
        }
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("wechatId", user.getWechatId());
        profile.put("signature", user.getSignature());
        profile.put("isOnline", user.getIsOnline());
        profile.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        return ResponseEntity.ok(profile);
    }

    /**
     * 更新个人信息（签名、微信号）
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Map<String, String> updates) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("Unauthorized"));
        }
        UserEntity user = userAuthService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(fail("User not found"));
        }
        if (updates.containsKey("signature")) {
            String sig = updates.get("signature");
            if (sig != null && sig.length() > 200) sig = sig.substring(0, 200);
            user.setSignature(sig);
        }
        if (updates.containsKey("wechatId")) {
            String wid = updates.get("wechatId");
            if (wid != null && !wid.trim().isEmpty()) {
                user.setWechatId(wid);
            }
        }
        userAuthService.saveUser(user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Profile updated");
        return ResponseEntity.ok(result);
    }

    /**
     * 上传头像
     */
    @PutMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestHeader("Authorization") String authHeader,
                                          @RequestParam("file") MultipartFile file) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(fail("Unauthorized"));
        }
        UserEntity user = userAuthService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(fail("User not found"));
        }
        Map<String, Object> uploadResult = fileUploadService.uploadFile(file);
        if (!Boolean.TRUE.equals(uploadResult.get("success"))) {
            return ResponseEntity.ok(uploadResult);
        }
        String fileUrl = (String) uploadResult.get("fileUrl");
        user.setAvatarUrl(fileUrl);
        userAuthService.saveUser(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Avatar uploaded");
        result.put("avatarUrl", fileUrl);
        return ResponseEntity.ok(result);
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
