package com.bfsforum.historyservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    private String userId;
    @Transient
    private String firstName;
    @Transient
    private String lastName;
    @Transient
    private String imgUrl;

    public UserInfo(String userId) {
        this.userId = userId;
    }
}
