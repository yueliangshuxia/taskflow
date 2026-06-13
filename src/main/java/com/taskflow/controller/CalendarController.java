package com.taskflow.controller;

import com.taskflow.dto.TaskDto;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final TaskService taskService;

    @GetMapping("/calendar")
    public String calendarView(Model model, Authentication auth) {
        model.addAttribute("title", "日历视图");
        model.addAttribute("content", "users/calendar");
        return "layout/base";
    }

    @GetMapping("/api/calendar/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            Authentication auth) {

        var tasks = taskService.findAccessibleTasks(auth.getName(),
                org.springframework.data.domain.PageRequest.of(0, 500,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "dueDate")));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return tasks.getContent().stream()
                .filter(t -> t.getDueDate() != null)
                .map(t -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", t.getId().toString());
                    event.put("title", t.getTitle());
                    event.put("start", t.getDueDate().format(formatter));
                    event.put("url", "/tasks/" + t.getId());
                    event.put("className", "calendar-event-" + t.getStatus().name().toLowerCase());
                    Map<String, Object> props = new HashMap<>();
                    props.put("status", t.getStatus().getDisplayName());
                    props.put("priority", t.getPriority().getDisplayName());
                    props.put("project", t.getProjectName() != null ? t.getProjectName() : "");
                    props.put("assignee", t.getAssigneeName() != null ? t.getAssigneeName() : "");
                    event.put("extendedProps", props);
                    return event;
                })
                .collect(Collectors.toList());
    }
}
