package com.bfsforum.postservice.domain;

public enum Operation {
  BAN,          //  Published -> Banned, ADMIN ONLY
  UNBAN,        //  Banned -> Published, ADMIN ONLY
  HIDE,         // Published -> Hidden, OWNER ONLY
  SHOW,         // Hidden -> Published,OWNER ONLY
  DELETE,       // Published -> Deleted, OWNER ONLY
  RECOVER,      // Deleted -> Published, ADMIN ONLY
  ARCHIVE,      // Published -> Archived, OWNER ONLY
  UNARCHIVE,    // Archived -> Published, OWNER ONLY
}
