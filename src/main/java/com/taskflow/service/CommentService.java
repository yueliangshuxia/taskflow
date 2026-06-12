package com.taskflow.service;

import com.taskflow.dto.CommentDto;
import java.util.List;

public interface CommentService {
    CommentDto addComment(Long taskId, String content, String username);
    List<CommentDto> getTaskComments(Long taskId);
    void deleteComment(Long commentId, String username);
}
