package com.taskflow.dao;

import com.taskflow.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
