package com.hms.controller;

import com.hms.model.User;
import com.hms.service.UserService;
import com.hms.util.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            User user = userService.login(body.get("username"), body.get("password"));
            // store safe copy (no password) in session
            session.setAttribute("currentUser", user);
            return ResponseEntity.ok(ApiResponse.ok("Login successful", Map.of(
                    "userId",   user.getUserId(),
                    "username", user.getUsername(),
                    "role",     user.getRole().name()
            )));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User created = userService.register(user);
            return ResponseEntity.ok(ApiResponse.ok("Registration successful",
                    Map.of("userId", created.getUserId(), "username", created.getUsername())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return ResponseEntity.status(401).body(ApiResponse.error("Not logged in"));
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "userId",   user.getUserId(),
                "username", user.getUsername(),
                "role",     user.getRole().name()
        )));
    }
}
