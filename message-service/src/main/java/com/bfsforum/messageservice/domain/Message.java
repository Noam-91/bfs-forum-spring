package com.bfsforum.messageservice.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Schema(name = "Message", description = "User sent message from Contact page", title = "Message")
public class Message {
  @Id
  @GeneratedValue(generator = "UUID")
  @Schema(type = "string", format = "uuid")
  private String id;

  @NotBlank
  @Schema(description = "User email", example = "ny@bfs.com")
  private String email;

  @Schema(description = "Message title", example = "I have a question")
  @NotBlank
  private String subject;

  @NotBlank
  @Schema(description = "Message body")
  private String content;

  @Schema(description = "Message status", example = "SOLVED, UNSOLVED")
  @Enumerated(EnumType.STRING)
  private Status status;

  @Schema(description = "Message created at", example = "2021-01-01 00:00:00")
  @Column(name = "created_at", insertable = false, updatable = false)
  private Timestamp createdAt;

  @Schema(description = "Message updated at", example = "2021-01-01 00:00:00")
  @Column(name = "updated_at", insertable = false)
  @Version
  private Timestamp updatedAt;

  @Schema(description = "Message updated by", type = "string", format = "uuid")
  @Column(name = "updated_by")
  private String updatedBy;
}
