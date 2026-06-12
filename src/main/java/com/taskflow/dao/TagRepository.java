package com.taskflow.dao;

import com.taskflow.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByProjectId(Long projectId);
    boolean existsByNameAndProjectId(String name, Long projectId);
}
