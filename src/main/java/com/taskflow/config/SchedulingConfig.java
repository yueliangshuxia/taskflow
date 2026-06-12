package com.taskflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulingConfig {
    // Scheduled tasks are in service/ScheduledTaskService.java
}
