package com.im;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "forward:/index-v2.html";
    }
}

@RestController
class ApiController {
    @GetMapping("/api/info")
    public Map<String, String> getInfo(HttpServletRequest request) {
        Map<String, String> info = new HashMap<>();
        info.put("name", "即时通讯系统");
        info.put("version", "2.0.0");
        info.put("description", "基于 Spring Boot 和 WebSocket 的即时通讯系统");
        String scheme = request.isSecure() ? "wss" : "ws";
        info.put("wsUrl", scheme + "://" + request.getServerName() + ":" + request.getServerPort() + "/im");
        return info;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis() + "");
        return health;
    }
}
