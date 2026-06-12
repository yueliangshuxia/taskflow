package com.taskflow.dao;

import com.taskflow.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @EntityGraph(attributePaths = {"sender", "recipient"})
    Page<Message> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender", "recipient"})
    java.util.Optional<Message> findWithGraphById(Long id);

    @EntityGraph(attributePaths = {"sender", "recipient"})
    Page<Message> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);

    long countByRecipientIdAndIsRead(Long recipientId, boolean isRead);

    @Query("SELECT m FROM Message m WHERE m.recipient.id = :userId OR m.sender.id = :userId ORDER BY m.createdAt DESC")
    @EntityGraph(attributePaths = {"sender", "recipient"})
    Page<Message> findUserMessages(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.id = :id AND m.recipient.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);
}
