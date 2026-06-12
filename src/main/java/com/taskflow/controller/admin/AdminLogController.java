package com.taskflow.controller.admin;

import com.taskflow.entity.AuditLog;
import com.taskflow.dao.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public String viewLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<AuditLog> logPage = auditLogRepository.findAll(pageable);

        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("title", "审计日志");
        model.addAttribute("content", "admin/logs");
        return "layout/base";
    }
}
