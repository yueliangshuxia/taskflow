package com.taskflow.dao;

import com.taskflow.entity.Task;
import com.taskflow.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    Page<Task> findByCreatorId(Long creatorId, Pageable pageable);

    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId OR t.creator.id = :userId")
    Page<Task> findAccessibleTasks(@Param("userId") Long userId, Pageable pageable);

    long countByCreatorIdAndStatus(Long creatorId, TaskStatus status);

    long countByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

    long countByCreatorId(Long creatorId);

    long countByAssigneeId(Long assigneeId);

    long countByProjectId(Long projectId);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countTasksByPriority();

    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status <> 'DONE'")
    List<Task> findOverdueTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId OR t.creator.id = :userId")
    long countAccessibleTasks(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE (t.creator.id = :userId OR t.assignee.id = :userId) AND t.status = :status")
    long countAccessibleTasksByStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId")
    List<Task> findAllByProjectId(@Param("projectId") Long projectId);

    @EntityGraph(attributePaths = {"project", "assignee", "creator"})
    Page<Task> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"project", "assignee", "creator"})
    Page<Task> findAllByStatusOrderByCreatedAtDesc(TaskStatus status, Pageable pageable);

    // Soft-delete queries
    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NOT NULL ORDER BY t.deletedAt DESC")
    List<Task> findDeleted();

    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NOT NULL AND t.project.id = :projectId ORDER BY t.deletedAt DESC")
    List<Task> findDeletedByProjectId(@Param("projectId") Long projectId);

    @Query("UPDATE Task t SET t.deletedAt = NULL WHERE t.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    void restoreById(@Param("id") Long id);

    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NOT NULL AND t.deletedAt < :cutoff")
    List<Task> findDeletedBefore(@Param("cutoff") LocalDateTime cutoff);
}
