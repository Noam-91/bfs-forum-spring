package com.bfsforum.emailservice.controller;

import com.bfsforum.emailservice.dto.EmailRequest;
import com.bfsforum.emailservice.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@Tag(name = "Email", description = "Endpoints for email activation")
public class EmailController {

  @Autowired
  private EmailService emailService;
  @Operation(summary = "Send activation email")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Activation email sent"),
          @ApiResponse(responseCode = "429", description = "Too many requests"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/send-activation")
  public ResponseEntity<String> sendActivationEmail(@Valid @RequestBody EmailRequest request) {
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


  @Operation(summary = "Activate user using token")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User activated"),
          @ApiResponse(responseCode = "401", description = "Invalid or expired token")
  })
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



