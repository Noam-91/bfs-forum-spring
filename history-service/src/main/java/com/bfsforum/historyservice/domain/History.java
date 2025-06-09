package com.bfsforum.historyservice.domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Column(name = "id")
    private String historyId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "post_id")
    private String postId;

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;

}
