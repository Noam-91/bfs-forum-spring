package com.bfsforum.authservice.controller;

import com.bfsforum.authservice.domain.User;
import com.bfsforum.authservice.dto.LoginRequest;
import com.bfsforum.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Login to the system.")
  public ResponseEntity<Map<String,String>> login(@RequestBody LoginRequest loginRequest,
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
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
  }
}
