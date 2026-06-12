package com.taskflow.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class TaskFilter {
    private String keyword;
    private String status;
    private String priority;
    private Long assigneeId;
    private Long projectId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDateFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDateTo;
}
