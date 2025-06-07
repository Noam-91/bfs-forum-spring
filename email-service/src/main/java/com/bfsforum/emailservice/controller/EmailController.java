package com.bfsforum.emailservice.controller;

import com.bfsforum.emailservice.dto.EmailRequest;
import com.bfsforum.emailservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
              .body(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Failed to send email: " + e.getMessage());
    }
  }



  @GetMapping("/activate")
  public ResponseEntity<String> activateUser(@RequestParam String token) {
    if (emailService.validateToken(token)) {
      String email = emailService.consumeToken(token);
      return ResponseEntity.ok("Account activated for " + email);
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
    }
  }

}



