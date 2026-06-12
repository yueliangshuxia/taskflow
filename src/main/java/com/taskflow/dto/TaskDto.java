package com.taskflow.dto;

import com.taskflow.entity.enums.TaskPriority;
import com.taskflow.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskDto {

    private Long id;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过200个字符")
    private String title;

    @Size(max = 5000, message = "任务描述不能超过5000个字符")
    private String description;

    private TaskStatus status;
    private TaskPriority priority;

    private Long projectId;
    private String projectName;

    private Long assigneeId;
    private String assigneeName;

    private String creatorName;

    private LocalDate dueDate;
}
