package com.bfsforum.messageservice.repository;

import com.bfsforum.messageservice.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface MessageDao extends JpaRepository<Message, String> {
  Optional<Message> findById(String id);
  List<Message> findAll(Pageable pageable);
  List<Message> findByUserId(String userId, Pageable pageable);
  List<Message> findAllByStatus(String status, Pageable pageable);
}
