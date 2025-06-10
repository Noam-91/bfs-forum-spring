package com.bfsforum.historyservice.repository;

import com.bfsforum.historyservice.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistoryRepo extends JpaRepository<History, String> {
    Optional<History> findByUserIdAndPostId(String userId, String postId);
    List<History> findByUserIdOrderByViewedAtDesc(String userId);
}
