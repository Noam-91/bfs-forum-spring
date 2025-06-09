package com.bfsforum.historyservice.repository;

import com.bfsforum.historyservice.domain.History;
import com.bfsforum.historyservice.domain.HistoryTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryTestRepo extends JpaRepository<HistoryTest, String> {
}