package com.bfsforum.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileFlatDto {
    private String id;
    private String username;
    private String role;
    @JsonProperty("isActive")
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String imgUrl;
    private Instant createdAt;
}
