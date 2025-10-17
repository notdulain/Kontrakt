package com.kontrakt.backend.controller;

import com.kontrakt.backend.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    // POST /api/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();

        response.put("message", "Login endpoint received credentials.");
        response.put("status", "pending");

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);

//        String username = credentials.get("username");
//        String password = credentials.get("password");


        // Simple validation (for testing only!)
//        if ("admin".equals(username) && "1234".equals(password)) {
//            response.put("success", true);
//            response.put("token", "abc123xyz789");
//            response.put("message", "Login successful");
//            return ResponseEntity.ok()
//                    .header("Content-Type", "application/json")
//                    .body(response);
//        } else {
//            response.put("success", false);
//            response.put("message", "Invalid credentials");
//            return ResponseEntity.status(401).body(response);
//        }
    }

    // GET /api/users/{id}
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("username", "user" + id);
        user.put("email", "user" + id + "@example.com");
        user.put("role", "USER");

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(user);
    }

    // PUT /api/users/{id}
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable int id,
            @RequestBody Map<String, String> updates) {

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("updated", true);
        response.put("role", updates.get("role"));
        response.put("message", "User updated successfully");

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("X-App", "TestLangDemo")
                .body(response);
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        // TODO: Implement user deletion logic
        response.put("id", id);
        response.put("message", "User deletion endpoint reached.");
        response.put("status", "pending");

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
    }

}
