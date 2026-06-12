package com.taskflow.controller.rest;

import com.taskflow.entity.Tag;
import com.taskflow.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagRestController {

    private final TagService tagService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Tag>> getProjectTags(@PathVariable Long projectId) {
        return ResponseEntity.ok(tagService.getProjectTags(projectId));
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<Tag> createTag(@PathVariable Long projectId,
                                          @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(tagService.createTag(
                projectId, body.get("name"), body.get("color")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/task/{taskId}/toggle/{tagId}")
    public ResponseEntity<Void> toggleTaskTag(@PathVariable Long taskId,
                                               @PathVariable Long tagId) {
        tagService.toggleTaskTag(taskId, tagId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/task/{taskId}/set")
    public ResponseEntity<Void> setTaskTags(@PathVariable Long taskId,
                                             @RequestBody List<Long> tagIds) {
        tagService.setTaskTags(taskId, tagIds);
        return ResponseEntity.ok().build();
    }
}
