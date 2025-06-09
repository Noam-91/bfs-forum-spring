package com.bfsforum.authservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
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

