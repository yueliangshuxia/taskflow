package com.taskflow.entity;

import com.taskflow.entity.enums.TaskPriority;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_template_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @ToString.Exclude
    private TaskTemplate template;
}
