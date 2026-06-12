package com.taskflow.service.impl;

import com.taskflow.dto.ProjectDto;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.dao.ProjectRepository;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.ProjectService;
import com.taskflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findAccessibleProjects(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<Project> projects = projectRepository.findAccessibleProjects(user.getId(), pageable);
        return projects.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto findById(Long projectId) {
        Project project = getProjectEntity(projectId);
        return convertToDto(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("项目不存在"));
    }

    @Override
    @Transactional
    public ProjectDto createProject(String name, String description, String username) {
        User owner = getUserByUsername(username);
        Project project = Project.builder()
                .name(name)
                .description(description)
                .owner(owner)
                .build();
        project.getMembers().add(owner);
        project = projectRepository.save(project);

        auditLogService.log("CREATE", "Project", project.getId(),
                "创建项目: " + name, username);

        return convertToDto(project);
    }

    @Override
    @Transactional
    public ProjectDto updateProject(Long projectId, String name, String description, String username) {
        Project project = getProjectEntity(projectId);
        checkOwner(project, username);

        project.setName(name);
        project.setDescription(description);
        project = projectRepository.save(project);
        return convertToDto(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, String username) {
        Project project = getProjectEntity(projectId);
        checkOwner(project, username);
        String projectName = project.getName();
        project.setDeletedAt(java.time.LocalDateTime.now());
        projectRepository.save(project);

        auditLogService.log("DELETE", "Project", projectId,
                "删除项目: " + projectName, username);
    }

    @Override
    @Transactional
    public void restoreProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("项目不存在或已永久删除"));
        project.setDeletedAt(null);
        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> findDeletedProjects() {
        return projectRepository.findDeleted().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public void addMember(Long projectId, Long userId, String username) {
        Project project = getProjectEntity(projectId);
        checkOwner(project, username);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        if (!project.getMembers().contains(user)) {
            project.getMembers().add(user);
            projectRepository.save(project);

            auditLogService.log("ADD_MEMBER", "Project", projectId,
                    "添加成员: " + user.getUsername() + " 到项目: " + project.getName(), username);
        }
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId, String username) {
        Project project = getProjectEntity(projectId);
        checkOwner(project, username);

        project.getMembers().removeIf(m -> m.getId().equals(userId));
        projectRepository.save(project);

        auditLogService.log("REMOVE_MEMBER", "Project", projectId,
                "移除成员 (userId=" + userId + ") 从项目: " + project.getName(), username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getProjectMembers(Long projectId) {
        Project project = getProjectEntity(projectId);
        return new ArrayList<>(project.getMembers());
    }

    private void checkOwner(Project project, String username) {
        if (!project.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("只有项目所有者才能执行此操作");
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setOwnerName(project.getOwner().getUsername());
        dto.setTaskCount(project.getTasks().size());
        dto.setMemberCount(project.getMembers().size());
        return dto;
    }
}
