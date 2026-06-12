package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {

    private Long id;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private String authorName;
    private String createdAt;
}
