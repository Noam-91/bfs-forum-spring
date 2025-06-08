package com.bfsforum.historyservice.domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private String historyId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private String userId;

    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    private String postId;

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;
    public History(String userId, String postId, LocalDateTime viewedAt) {

        this.userId   = userId;
        this.postId   = postId;
        this.viewedAt = viewedAt;
    }

}
