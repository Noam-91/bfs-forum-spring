package com.bfsforum.messageservice.exception;

public class NotAuthorizedException extends RuntimeException{
    public NotAuthorizedException(String message) {
        super(message);
    }
}
