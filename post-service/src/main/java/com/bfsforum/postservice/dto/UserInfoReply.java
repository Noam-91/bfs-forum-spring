package com.bfsforum.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoReply {
  private String userId;
  private String username;
  private String firstName;
  private String lastName;
  //todo: add imgUrl
}
