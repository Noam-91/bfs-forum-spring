package com.bfsforum.userservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String imgUrl;
}
