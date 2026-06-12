package com.taskflow.dao;

import com.taskflow.entity.VisitLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

    long countByVisitedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT v.pageUrl, COUNT(v) FROM VisitLog v GROUP BY v.pageUrl ORDER BY COUNT(v) DESC")
    Page<Object[]> findTopVisitedPages(Pageable pageable);

    long countByUsernameAndVisitedAtBetween(String username, LocalDateTime start, LocalDateTime end);

    int deleteByVisitedAtBefore(LocalDateTime dateTime);
}
