package com.bfsforum.historyservice.domain;

public enum PostStatus {
    UNPUBLISHED,  // draft
    PUBLISHED,    // published
    HIDDEN,       // setup by user
    BANNED,       // banned by admin
    ARCHIVED,     // archived by user
    DELETED;      // deleted

    public static boolean isPostStatus(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return false;
        }
        try {
            PostStatus.valueOf(statusString.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
