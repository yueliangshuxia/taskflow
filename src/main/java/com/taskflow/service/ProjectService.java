package com.taskflow.service;

import com.taskflow.dto.ProjectDto;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    Page<ProjectDto> findAccessibleProjects(String username, Pageable pageable);
    ProjectDto findById(Long projectId);
    Project getProjectEntity(Long projectId);
    ProjectDto createProject(String name, String description, String username);
    ProjectDto updateProject(Long projectId, String name, String description, String username);
    void deleteProject(Long projectId, String username);
    void addMember(Long projectId, Long userId, String username);
    void removeMember(Long projectId, Long userId, String username);
    List<User> getProjectMembers(Long projectId);
}
