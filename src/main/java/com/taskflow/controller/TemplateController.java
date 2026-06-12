package com.taskflow.controller;

import com.taskflow.entity.TaskTemplate;
import com.taskflow.entity.TaskTemplateItem;
import com.taskflow.service.TaskTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TaskTemplateService templateService;

    @PostMapping("/create")
    public String createTemplate(@PathVariable Long projectId,
                                  @RequestParam String name,
                                  @RequestParam(required = false) String description,
                                  @RequestParam List<String> itemTitles,
                                  RedirectAttributes redirectAttrs) {
        try {
            List<TaskTemplateItem> items = itemTitles.stream()
                    .filter(t -> !t.isBlank())
                    .map(title -> TaskTemplateItem.builder().title(title).build())
                    .toList();
            templateService.createTemplate(projectId, name, description, items);
            redirectAttrs.addFlashAttribute("message", "模板创建成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "模板创建失败: " + e.getMessage());
        }
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{templateId}/apply")
    public String applyTemplate(@PathVariable Long projectId,
                                 @PathVariable Long templateId,
                                 Authentication auth,
                                 RedirectAttributes redirectAttrs) {
        try {
            int count = templateService.applyTemplate(projectId, templateId, auth.getName());
            redirectAttrs.addFlashAttribute("message", "已从模板创建 " + count + " 个任务");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "应用模板失败: " + e.getMessage());
        }
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{templateId}/delete")
    public String deleteTemplate(@PathVariable Long projectId,
                                  @PathVariable Long templateId,
                                  RedirectAttributes redirectAttrs) {
        try {
            templateService.deleteTemplate(templateId);
            redirectAttrs.addFlashAttribute("message", "模板已删除");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "删除模板失败");
        }
        return "redirect:/projects/" + projectId;
    }
}
