package com.taskflow.service.impl;

import com.taskflow.dto.TaskCreateDto;
import com.taskflow.dto.TaskDto;
import com.taskflow.dto.TaskFilter;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.entity.enums.TaskPriority;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.dao.TaskRepository;
import com.taskflow.dao.UserRepository;
import com.taskflow.dao.ProjectRepository;
import com.taskflow.service.TaskService;
import com.taskflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> findAccessibleTasks(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<Task> tasks = taskRepository.findAccessibleTasks(user.getId(), pageable);
        return tasks.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> findByProjectId(Long projectId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByProjectId(projectId, pageable);
        return tasks.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> findFiltered(Long projectId, TaskFilter filter, Pageable pageable) {
        Specification<Task> spec = TaskSpecification.withFilter(filter);
        if (projectId != null) {
            spec = spec.and(TaskSpecification.hasProjectId(projectId));
        }
        Page<Task> tasks = taskRepository.findAll(spec, pageable);
        return tasks.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto findById(Long taskId) {
        Task task = getTaskEntity(taskId);
        return convertToDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskEntity(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
    }

    @Override
    @Transactional
    public TaskDto createTask(Long projectId, TaskCreateDto createDto, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("项目不存在"));
        User creator = getUserByUsername(username);

        Task task = Task.builder()
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .project(project)
                .creator(creator)
                .status(TaskStatus.TODO)
                .priority(createDto.getPriority() != null ?
                        TaskPriority.valueOf(createDto.getPriority()) : TaskPriority.MEDIUM)
                .dueDate(createDto.getDueDate())
                .build();

        if (createDto.getAssigneeId() != null) {
            User assignee = userRepository.findById(createDto.getAssigneeId())
                    .orElse(null);
            task.setAssignee(assignee);
        }

        task = taskRepository.save(task);

        auditLogService.log("CREATE", "Task", task.getId(),
                "在项目 " + project.getName() + " 中创建任务: " + task.getTitle(), username);

        return convertToDto(task);
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long taskId, TaskCreateDto updateDto, String username) {
        Task task = getTaskEntity(taskId);

        task.setTitle(updateDto.getTitle());
        task.setDescription(updateDto.getDescription());

        if (updateDto.getPriority() != null) {
            task.setPriority(TaskPriority.valueOf(updateDto.getPriority()));
        }
        task.setDueDate(updateDto.getDueDate());

        if (updateDto.getAssigneeId() != null) {
            userRepository.findById(updateDto.getAssigneeId())
                    .ifPresent(task::setAssignee);
        } else {
            task.setAssignee(null);
        }

        task = taskRepository.save(task);
        return convertToDto(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, String username) {
        Task task = getTaskEntity(taskId);
        String taskTitle = task.getTitle();
        Long projectId = task.getProject() != null ? task.getProject().getId() : null;
        task.setDeletedAt(java.time.LocalDateTime.now());
        taskRepository.save(task);

        auditLogService.log("DELETE", "Task", taskId,
                "删除任务: " + taskTitle + " (projectId=" + projectId + ")", username);
    }

    @Override
    @Transactional
    public void restoreTask(Long taskId) {
        taskRepository.restoreById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> findDeletedTasks() {
        return taskRepository.findDeleted().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public TaskDto updateStatus(Long taskId, TaskStatus newStatus, String username) {
        Task task = getTaskEntity(taskId);

        if (task.getStatus() == TaskStatus.DONE && newStatus != TaskStatus.DONE) {
            throw new BadRequestException("无法回退已完成的任務");
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        task = taskRepository.save(task);

        auditLogService.log("UPDATE_STATUS", "Task", taskId,
                "任务 \"" + task.getTitle() + "\" 状态变更: " + oldStatus.getDisplayName()
                        + " → " + newStatus.getDisplayName(), username);

        return convertToDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> findAllByProjectId(Long projectId) {
        return taskRepository.findAllByProjectId(projectId).stream()
                .map(this::convertToDto)
                .toList();
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        dto.setProjectName(task.getProject() != null ? task.getProject().getName() : null);
        dto.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        dto.setAssigneeName(task.getAssignee() != null ? task.getAssignee().getDisplayName() : null);
        dto.setCreatorName(task.getCreator().getDisplayName());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setTags(List.copyOf(task.getTags()));
        return dto;
    }
}
