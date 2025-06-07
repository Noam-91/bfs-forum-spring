package com.bfsforum.authservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "User", description = "Represents a user in the system", title = "User")
public class User {
  @Id
  @Schema(type = "string", format = "uuid")
  private String id;

  @Schema(type = "string", description = "Email", example = "ny@bfs.com")
  private String username;

  @Schema(type = "string")
  @JsonIgnore
  private String password;

  @Schema(type = "string", description = "Role", example = "UNVERIFIED, USER, ADMIN, SUPER_ADMIN")
  private String role;

  @Column (name = "is_active")
  private Boolean isActive;
}
