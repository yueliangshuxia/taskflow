package com.taskflow.service;

import com.taskflow.entity.TaskAttachment;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    TaskAttachment storeFile(MultipartFile file, Long taskId, Long userId);
    void deleteFile(Long attachmentId);
    void deleteAllByTaskId(Long taskId);
}
