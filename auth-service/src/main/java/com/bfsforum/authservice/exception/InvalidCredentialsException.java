package com.bfsforum.authservice.exception;

public class InvalidCredentialsException extends RuntimeException {
    private final String message;
    public InvalidCredentialsException(String message) {
        super(message);
        this.message = message;
    }
    @Override
    public String getMessage() {
        return message;
    }
}
