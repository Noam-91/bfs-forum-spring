package com.bfsforum.emailservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String imgUrl;
}
