package com.bfsforum.historyserivce.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class History {
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID historyId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    private UUID postId;

    @Column(name="viewed_at")
    private LocalDateTime viewedAt;
    public History(UUID userId, UUID postId, LocalDateTime viewedAt) {
        this.userId   = userId;
        this.postId   = postId;
        this.viewedAt = viewedAt;
    }

}
