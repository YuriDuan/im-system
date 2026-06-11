package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            return fail("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            return fail("Email already exists");
        }
        if (username.length() < 2 || username.length() > 20) {
            return fail("Username length must be between 2 and 20 characters");
        }
        if (password.length() < 6) {
            return fail("Password must be at least 6 characters");
        }

        try {
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setIsOnline(false);
            user.setWechatId(generateWechatId(username));
            user.setSignature("");
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Registration successful");
            result.put("userId", user.getId());
            return result;
        } catch (Exception e) {
            return fail("Registration failed: " + e.getMessage());
        }
    }

    public Map<String, Object> login(String username, String password) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return fail("Invalid username or password");
        }

        UserEntity user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return fail("Invalid username or password");
        }

        String token = jwtUtil.generateToken(username, user.getId());
        user.setIsOnline(true);
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Login successful");
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    public Map<String, Object> validateToken(String token) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("valid", false);
            result.put("message", "Token is invalid or expired");
            return result;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            result.put("valid", false);
            result.put("message", "User not found");
            return result;
        }

        result.put("valid", true);
        result.put("username", username);
        result.put("userId", userOpt.get().getId());
        return result;
    }

    public Map<String, Object> logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setIsOnline(false);
            userRepository.save(user);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Logout successful");
        return result;
    }

    public UserEntity getUser(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }

    public Map<String, Object> changePassword(String username, String oldPassword, String newPassword) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return fail("User not found");
        }

        UserEntity user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return fail("Old password is incorrect");
        }
        if (newPassword.length() < 6) {
            return fail("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Password updated successfully");
        return result;
    }

    private String generateWechatId(String username) {
        String base = username.replaceAll("[^a-zA-Z0-9]", "");
        if (base.length() > 8) base = base.substring(0, 8);
        if (base.isEmpty()) base = "user";
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return base + "_" + suffix;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
