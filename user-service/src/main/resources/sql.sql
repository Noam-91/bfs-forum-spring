CREATE DATABASE IF NOT EXISTS bfsforum;
USE bfsforum;

DROP TABLE IF EXISTS user;
CREATE TABLE user (
                      id VARCHAR(36) PRIMARY KEY,
                      username VARCHAR(255) UNIQUE NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      is_active BOOLEAN DEFAULT FALSE,
                      role ENUM('UNVERIFIED', 'USER', 'ADMIN', 'SUPER_ADMIN') NOT NULL
);

DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile (
                              id VARCHAR(36) PRIMARY KEY,
                              user_id VARCHAR(36) NOT NULL,
                              is_active BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              img_url VARCHAR(2048),
                              first_name VARCHAR(255),
                              last_name VARCHAR(255)
);

DROP TABLE IF EXISTS message;
CREATE TABLE message (
                         id VARCHAR(36) PRIMARY KEY,
                         email VARCHAR(255) NOT NULL,
                         subject VARCHAR(255) NOT NULL,
                         content TEXT NOT NULL,
                         status ENUM('SOLVED', 'UNSOLVED') NOT NULL DEFAULT 'UNSOLVED',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                         updated_by VARCHAR(36)
);

DROP TABLE IF EXISTS history;
CREATE TABLE history (
                         id VARCHAR(36) PRIMARY KEY,
                         user_id VARCHAR(36) NOT NULL,
                         post_id VARCHAR(36) NOT NULL,
                         viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT unique_user_post UNIQUE (user_id, post_id)
);

DROP TABLE IF EXISTS verification;
CREATE TABLE verification (
                              token VARCHAR(36) PRIMARY KEY,
                              user_id VARCHAR(36) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              expired_at TIMESTAMP
);

DELIMITER //
CREATE TRIGGER set_expired_at_before_insert
    BEFORE INSERT ON verification
    FOR EACH ROW
BEGIN
    IF NEW.expired_at IS NULL THEN
        SET NEW.expired_at = CURRENT_TIMESTAMP + INTERVAL 1 DAY;
END IF;
END;
//
DELIMITER ;

