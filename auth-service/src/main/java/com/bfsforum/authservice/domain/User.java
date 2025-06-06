package com.bfsforum.authservice.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(generator = "UUID")
  @Schema(type = "string", format = "uuid")
  private String id;

  @Schema(type = "string", description = "Email", example = "ny@bfs.com")
  private String username;

  @Schema(type = "string")
  private String password;

  @Schema(type = "string", description = "Role", example = "UNVERIFIED, USER, ADMIN, SUPER_ADMIN")
  private String role;

  @Column (name = "is_active")
  private Boolean isActive;
}
