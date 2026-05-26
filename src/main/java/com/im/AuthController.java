package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserAuthService userAuthService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");
        String email = request.get("email");

        if (isBlank(username) || isBlank(password) || isBlank(email)) {
            return ResponseEntity.badRequest().body(error("Username, password and email are required"));
        }
        if (!isBlank(confirmPassword) && !password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(error("Passwords do not match"));
        }

        return ResponseEntity.ok(userAuthService.register(username.trim(), password, email.trim()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (isBlank(username) || isBlank(password)) {
            return ResponseEntity.badRequest().body(error("Username and password are required"));
        }

        return ResponseEntity.ok(userAuthService.login(username.trim(), password));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader(value = "Authorization", required = false) String token) {
        if (isBlank(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "Token cannot be empty");
            return ResponseEntity.badRequest().body(result);
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return ResponseEntity.ok(userAuthService.validateToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (isBlank(username)) {
            return ResponseEntity.badRequest().body(error("Username is required"));
        }

        return ResponseEntity.ok(userAuthService.logout(username.trim()));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        UserEntity user = userAuthService.getUser(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("isOnline", user.getIsOnline());
        userInfo.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (isBlank(username) || isBlank(oldPassword) || isBlank(newPassword)) {
            return ResponseEntity.badRequest().body(error("All fields are required"));
        }

        return ResponseEntity.ok(userAuthService.changePassword(username.trim(), oldPassword, newPassword));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
