package com.bfsforum.userservice.dto;

import com.bfsforum.userservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String imgUrl;
    private boolean isActive;
    private Role role;
}