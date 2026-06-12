package com.taskflow.service.impl;

import com.taskflow.dto.CommentDto;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.dao.CommentRepository;
import com.taskflow.dao.TaskRepository;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.CommentService;
import com.taskflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public CommentDto addComment(Long taskId, String content, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Comment comment = Comment.builder()
                .content(content)
                .task(task)
                .author(author)
                .build();

        comment = commentRepository.save(comment);
        return convertToDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getTaskComments(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在"));
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("只能删除自己的评论");
        }
        Long taskId = comment.getTask().getId();
        commentRepository.delete(comment);

        auditLogService.log("DELETE", "Comment", commentId,
                "删除评论 (taskId=" + taskId + ")", username);
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorName(comment.getAuthor().getDisplayName());
        dto.setCreatedAt(comment.getCreatedAt() != null ?
                comment.getCreatedAt().toString() : "");
        return dto;
    }
}
