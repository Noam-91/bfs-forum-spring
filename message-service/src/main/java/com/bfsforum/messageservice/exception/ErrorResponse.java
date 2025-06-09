package com.bfsforum.messageservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Value
@Builder
public class ErrorResponse {
  @Builder.Default
  LocalDateTime timestamp = LocalDateTime.now();;
  String path;
  int status;
  String error;
  @Builder.Default
  String requestId = UUID.randomUUID().toString();;

}

