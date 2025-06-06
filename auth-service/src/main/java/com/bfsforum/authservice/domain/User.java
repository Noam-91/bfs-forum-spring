package com.bfsforum.authservice.domain;

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
  private String id;
  private String username;
  private String password;
  private String role;
  @Column (name = "is_active")
  private Boolean isActive;
  @Column(name = "created_at", insertable = false)
  private Timestamp createdAt;
  @Column(name = "updated_at", insertable = false)
  private Timestamp updatedAt;
  @Column(name = "updated_by")
  private Long updatedBy;
}
