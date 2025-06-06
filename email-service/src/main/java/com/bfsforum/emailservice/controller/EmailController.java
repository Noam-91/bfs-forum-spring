package com.bfsforum.emailservice.controller;

import com.bfsforum.emailservice.service.EmailService;
import com.bfsforum.emailservice.dto.EmailRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/email")
public class EmailController {

  @Autowired
  private EmailService emailService;

  @PostMapping("/send-activation")
  public ResponseEntity<String> sendActivationEmail(@RequestBody EmailRequest request) {
    try {
      emailService.sendActivationEmail(request.getEmail());
      return ResponseEntity.ok("Activation email sent.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email.");
    }
  }

  @GetMapping("/activate")
  public ResponseEntity<String> activateUser(@RequestParam String token) {
    if (emailService.validateToken(token)) {
      String email = emailService.consumeToken(token);
      URI redirect = URI.create("http://yourfrontend.com/home");
      return ResponseEntity.status(HttpStatus.FOUND).location(redirect).build();
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
    }
  }
}



