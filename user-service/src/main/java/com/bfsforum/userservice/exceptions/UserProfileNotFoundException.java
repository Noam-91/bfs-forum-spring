package com.bfsforum.userservice.exceptions;

import java.util.UUID;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException (String message) {
        super(message);
    }
}
