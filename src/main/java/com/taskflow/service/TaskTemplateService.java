package com.taskflow.service;

import com.taskflow.dao.*;
import com.taskflow.entity.*;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskTemplateService {

    private final TaskTemplateRepository templateRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<TaskTemplate> getProjectTemplates(Long projectId) {
        return templateRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    @Transactional
    public TaskTemplate createTemplate(Long projectId, String name, String description,
                                        List<TaskTemplateItem> items) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("项目不存在"));

        TaskTemplate template = TaskTemplate.builder()
                .name(name)
                .description(description)
                .project(project)
                .build();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                TaskTemplateItem item = items.get(i);
                item.setTemplate(template);
                item.setSortOrder(i);
            }
            template.setItems(items);
        }

        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        templateRepository.deleteById(templateId);
    }

    @Transactional
    public int applyTemplate(Long projectId, Long templateId, String username) {
        TaskTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("模板不存在"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("项目不存在"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        int count = 0;
        for (TaskTemplateItem item : template.getItems()) {
            Task task = Task.builder()
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .priority(item.getPriority())
                    .status(TaskStatus.TODO)
                    .project(project)
                    .creator(user)
                    .build();
            taskRepository.save(task);
            count++;
        }
        return count;
    }

    private final UserRepository userRepository;
}
