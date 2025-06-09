package com.bfsforum.historyservice.domain;


/**
 * @author luluxue
 * @date 2025-06-06
 */

public enum PostStatus {
    UNPUBLISHED,  // draft
    PUBLISHED,    // published
    HIDDEN,       // setup by user
    BANNED,       // banned by admin
    DELETED       // deleted
}
