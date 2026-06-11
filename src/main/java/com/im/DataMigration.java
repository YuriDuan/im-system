package com.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

/**
 * 存量数据迁移 — 为已有用户补齐 wechatId 和 signature
 */
@Component
public class DataMigration {
    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void migrate() {
        List<UserEntity> users = userRepository.findAll();
        for (UserEntity user : users) {
            boolean changed = false;
            if (user.getWechatId() == null || user.getWechatId().trim().isEmpty()) {
                String base = user.getUsername().replaceAll("[^a-zA-Z0-9]", "");
                if (base.length() > 8) base = base.substring(0, 8);
                if (base.isEmpty()) base = "user";
                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                user.setWechatId(base + "_" + suffix);
                changed = true;
            }
            if (user.getSignature() == null) {
                user.setSignature("");
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        }
    }
}
