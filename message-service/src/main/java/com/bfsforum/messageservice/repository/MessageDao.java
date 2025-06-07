package com.bfsforum.messageservice.repository;

import com.bfsforum.messageservice.domain.Message;
import com.bfsforum.messageservice.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface MessageDao extends JpaRepository<Message, String> {
}
