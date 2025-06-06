package com.bfsforum.userservice.exceptions;

import java.util.UUID;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException (UUID id) {
        super("User profile not found with ID: " + id);
    }
}
