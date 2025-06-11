package com.bfsforum.userservice.controller;

import com.bfsforum.userservice.dto.EmailVerificationReply;
import com.bfsforum.userservice.dto.UserProfileDto;
import com.bfsforum.userservice.dto.UserProfileResponse;
import com.bfsforum.userservice.dto.UserRegisterMessage;
import com.bfsforum.userservice.entity.Role;
import com.bfsforum.userservice.entity.User;
import com.bfsforum.userservice.entity.UserProfile;
import com.bfsforum.userservice.exceptions.UserNotFoundException;
import com.bfsforum.userservice.exceptions.UserProfileNotFoundException;
import com.bfsforum.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/users")
@Tag(name = "User Service", description = "User registration and profile APIs")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user and store profile info.")
    @ApiResponse(responseCode = "200", description = "User registered successfully",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"message\": \"User registered successfully\" }")))
    @ApiResponse(responseCode = "400", description = "Username already exists",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"message\": \"Username already exists\" }")))
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegisterMessage request) {
        if (userService.usernameExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Username already exists"));
        }
        User user = userService.register(request);

        return ResponseEntity.ok(Map.of("message", "User registered successfully",
                // todo: might delete this line, only for demo purpose
                "userId", user.getId().toString()
        ));
    }


    @GetMapping("/verify")
    @Operation(summary = "Email verification", description = "Activate user verification based on the token in the email verification link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verification successful"),
            @ApiResponse(responseCode = "400", description = "Token is invalid or verification failed")
    })
    public ResponseEntity<?> verify(@RequestParam String token) {
        try {
            EmailVerificationReply reply = userService.verifyToken(token);

            if (reply == null || reply.getToken() == null) {
                return ResponseEntity.badRequest().body("Token invalid or not found");
            }

            User user = userService.findById(reply.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            userService.activateVerifiedUser(reply.getUserId(), reply.getExpiredAt());
            return ResponseEntity.ok(Map.of(
                    "message", "Email verification successful, welcome!",
                    "user", Map.of("firstName", user.getProfile().getFirstName())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }


    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get user profile", description = "Retrieve profile of the user by ID.")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"username\": \"josh123\", \"firstName\": \"Josh\", \"lastName\": \"Hu\", \"imgUrl\": \"...\", \"role\": \"USER\", \"isActive\": true }")))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{ \"message\": \"User not found\" }")))
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            UserProfileResponse response = UserProfileResponse.builder()
                    .username(user.getUsername())
                    .firstName(user.getProfile().getFirstName())
                    .lastName(user.getProfile().getLastName())
                    .imgUrl(user.getProfile().getImgUrl())
                    .isActive(user.isActive())
                    .role(user.getRole())
                    .build();

            return ResponseEntity.ok(response);

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "Update user profile", description = "Update profile information for a given user.")
    public ResponseEntity<Map<String, String>> updateProfile(@PathVariable String userId,
                                                             @Valid @RequestBody UserProfileDto dto) {
        try {
            userService.updateProfile(userId, dto);
            return ResponseEntity.ok(Map.of("message", "User profile updated successfully"));
        } catch (UserNotFoundException | UserProfileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/activation")
    @Operation(
        summary = "Toggle user activation (admin only)",
        description = "Admin can activate or deactivate a user by setting isActive = true/false"
    )
    public ResponseEntity<Map<String, String>> toggleUserActivation(
        @PathVariable String userId,
        @RequestBody Map<String, Boolean> request) {

        boolean isActive = request.getOrDefault("isActive", false);
        userService.setUserActivation(userId, isActive);
        String message = isActive ? "User activated" : "User deactivated";
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/{userId}/role")
    @Operation(summary = "Update user role", description = "Change a user's role (e.g. promote to admin).")
    public ResponseEntity<Map<String, String>> updateUserRole(@PathVariable String userId,
                                                              @RequestParam Role role) {
        try {
            userService.updateUserRole(userId, role);
            return ResponseEntity.ok(Map.of("message", "User role updated to " + role));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/page")
    @Operation(summary = "Get paginated users", description = "Retrieve paginated list of users with optional filters.")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role
    ) {
        try {
            Page<User> users = userService.getAllUsers(page, size, username, role);

            List<Map<String, Object>> mergedUsers = users.getContent().stream().map(user -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("role", user.getRole().name());
                data.put("isActive", user.isActive());

                UserProfile profile = user.getProfile();
                if (profile != null) {
                    data.put("firstName", profile.getFirstName());
                    data.put("lastName", profile.getLastName());
                    data.put("imgUrl", profile.getImgUrl());
                    data.put("createdAt", profile.getCreatedAt());
                }

                return data;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("content", mergedUsers);
            response.put("number", users.getNumber());
            response.put("size", users.getSize());
            response.put("totalPages", users.getTotalPages());
            response.put("totalElements", users.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }
}
