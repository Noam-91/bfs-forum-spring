package com.bfsforum.emailservice.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Column(name = "is_active")
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    private Role role;


}

