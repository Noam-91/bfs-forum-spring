package com.bfsforum.authservice.controller;

import com.bfsforum.authservice.domain.User;
import com.bfsforum.authservice.dto.LoginRequest;
import com.bfsforum.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Login to the system.")
  @ApiResponse(responseCode = "200", description = "Login successful",
      content = @Content(mediaType = "application/json",
      examples = @ExampleObject(value = "{ \"message\": \"Login Successfully\" }")))
  @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid credentials",
      content = @Content(mediaType = "application/json",
      examples = @ExampleObject(value = "{ \"message\": \"Invalid credentials\" }")))
  public ResponseEntity<Map<String,String>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                  HttpServletResponse response){
    try{
      String token = authService.loginAndIssueToken(loginRequest);
      // Save token into httpOnly cookie.
      Cookie cookie = new Cookie("token", token);
      cookie.setHttpOnly(true);
      cookie.setPath("/");
      cookie.setMaxAge(36000);          // 10 hours
      response.addCookie(cookie);
      return ResponseEntity.ok(Map.of("message", "Login Successfully"));
    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout", description = "Logout from the system.")
  @ApiResponse(responseCode = "200", description = "Logout successful",
      content = @Content(mediaType = "application/json",
      examples = @ExampleObject(value = "{ \"message\": \"Logout Successfully\" }")))
  public ResponseEntity<Map<String,String>> logout(HttpServletResponse response){
    Cookie cookie = new Cookie("token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    return ResponseEntity.ok(Map.of("message", "Logout Successfully"));
  }
  
  @GetMapping("")
  @Operation(summary = "Check Auth", description = "Validate the JWT and return User")
  @ApiResponse(responseCode = "200", description = "User Validated",
      content = @Content(mediaType = "application/json",
      schema = @Schema(implementation = User.class) ))
  @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid credentials",
      content = @Content(mediaType = "application/json",
      examples = @ExampleObject(value = "{ \"message\": \"Invalid credentials\" }")))
  public ResponseEntity<?> checkAuth(
      HttpServletResponse response,
      @Parameter(
          name = "userId",
          description = "The ID of the authenticated user, stored in request header",
          required = true,
          schema = @Schema(type = "string", format = "uuid", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"))
      @RequestHeader(value = "X-User-Id") String userId){
    try{
      User user = authService.findUserById(userId);
      return ResponseEntity.ok(user);
    } catch (RuntimeException e){
      // If user not found, clear the cookie and return 401
      log.info("User not found: {}", userId);
      Cookie cookie = new Cookie("token", null);
      cookie.setHttpOnly(true);
      cookie.setPath("/");
      cookie.setMaxAge(0);
      response.addCookie(cookie);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
  }
}
