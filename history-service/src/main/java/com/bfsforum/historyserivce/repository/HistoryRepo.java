package com.bfsforum.historyserivce.repository;

import com.bfsforum.historyserivce.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistoryRepo extends JpaRepository<History, UUID> {
    Optional<History> findByUserIdAndPostId(UUID userId, UUID postId);
    List<History> findByUserIdOrderByViewedAtDesc(UUID userId);
}
