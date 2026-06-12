package com.taskflow.dao;

import com.taskflow.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    Page<Project> findMemberProjects(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)")
    Page<Project> findAccessibleProjects(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT p) FROM Project p LEFT JOIN p.members m WHERE p.owner.id = :userId OR m.id = :userId")
    long countAccessibleProjects(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"owner"})
    Page<Project> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
