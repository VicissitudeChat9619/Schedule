package com.myschedule.controller;

import com.myschedule.dto.ApiResponse;
import com.myschedule.dto.BindQQRequest;
import com.myschedule.entity.User;
import com.myschedule.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> profile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/bind-qq")
    public ResponseEntity<ApiResponse<Void>> bindQQ(Authentication authentication,
                                                     @Valid @RequestBody BindQQRequest request) {
        User user = (User) authentication.getPrincipal();
        try {
            userService.bindQQ(user.getId(), request.getQqNumber());
            return ResponseEntity.ok(ApiResponse.success());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
