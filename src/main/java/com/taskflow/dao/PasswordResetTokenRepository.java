package com.taskflow.dao;

import com.taskflow.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
