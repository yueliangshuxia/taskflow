package com.taskflow.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskCreateDto {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private Long assigneeId;
    private LocalDate dueDate;
}
