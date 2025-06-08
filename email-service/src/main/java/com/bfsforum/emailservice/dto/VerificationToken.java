package com.bfsforum.emailservice.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Data
@Entity
@Table(name = "verification")
public class VerificationToken {

    @Id
    @Column(length = 36)
    private String token;
    @UuidGenerator
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expired_at")
    private Instant expiredAt;


}
