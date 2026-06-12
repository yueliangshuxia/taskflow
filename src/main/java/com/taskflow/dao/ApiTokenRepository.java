package com.taskflow.dao;

import com.taskflow.entity.ApiToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<ApiToken> findByToken(String token);
    List<ApiToken> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}
