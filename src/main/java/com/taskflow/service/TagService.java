package com.taskflow.service;

import com.taskflow.entity.Tag;
import com.taskflow.entity.Task;
import com.taskflow.entity.Project;
import com.taskflow.dao.TagRepository;
import com.taskflow.dao.TaskRepository;
import com.taskflow.dao.ProjectRepository;
import com.taskflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<Tag> getProjectTags(Long projectId) {
        return tagRepository.findByProjectId(projectId);
    }

    @Transactional
    public Tag createTag(Long projectId, String name, String color) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("项目不存在"));
        Tag tag = Tag.builder()
                .name(name)
                .color(color != null ? color : "#4f46e5")
                .project(project)
                .build();
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long tagId) {
        tagRepository.deleteById(tagId);
    }

    @Transactional
    public void toggleTaskTag(Long taskId, Long tagId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));

        Set<Tag> tags = task.getTags();
        if (tags.contains(tag)) {
            tags.remove(tag);
        } else {
            tags.add(tag);
        }
        taskRepository.save(task);
    }

    @Transactional
    public void setTaskTags(Long taskId, List<Long> tagIds) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        Set<Tag> tags = tagRepository.findAllById(tagIds).stream()
                .collect(java.util.stream.Collectors.toSet());
        task.setTags(tags);
        taskRepository.save(task);
    }
}
