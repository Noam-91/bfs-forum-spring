package com.bfsforum.emailservice.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class TokenInfo {
  private String email;
  private Instant createdAt;
}
