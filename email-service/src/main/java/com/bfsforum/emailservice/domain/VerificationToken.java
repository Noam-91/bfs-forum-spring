package com.bfsforum.emailservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Data
@Entity
@Table(name = "verification")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationToken {

  @Id
  private String token;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @JsonIgnore
  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expired_at", insertable = false, updatable = false)
  private Instant expiredAt;
}
