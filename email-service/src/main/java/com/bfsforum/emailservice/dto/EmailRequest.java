package com.bfsforum.emailservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailRequest {
  @NotBlank(message = "Email must not be blank")
  @Email(message = "Invalid email format")
  private String email;

}
