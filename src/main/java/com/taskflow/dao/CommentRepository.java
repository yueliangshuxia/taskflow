package com.taskflow.dao;

import com.taskflow.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    Page<Comment> findByTaskId(Long taskId, Pageable pageable);
    long countByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
