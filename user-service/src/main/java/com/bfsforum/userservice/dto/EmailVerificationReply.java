package com.bfsforum.userservice.dto;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailVerificationReply {
    private String token;
    private String userId;
    private Instant createAt;
    private Instant expiredAt;
}
