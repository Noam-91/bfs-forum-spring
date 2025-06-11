package com.bfsforum.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoReply {
    private String userId;
    private String firstName;
    private String lastName;
    private String imgUrl;
}