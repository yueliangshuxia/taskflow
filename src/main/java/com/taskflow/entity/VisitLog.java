package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 50)
    private String username;

    @Column(name = "visited_at", updatable = false)
    private LocalDateTime visitedAt;

    @PrePersist
    protected void onCreate() {
        visitedAt = LocalDateTime.now();
    }
}
