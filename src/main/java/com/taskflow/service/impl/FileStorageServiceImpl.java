package com.taskflow.service.impl;

import com.taskflow.entity.TaskAttachment;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.FileStorageException;
import com.taskflow.dao.TaskAttachmentRepository;
import com.taskflow.dao.TaskRepository;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TaskAttachment storeFile(MultipartFile file, Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String originalFileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + "." + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetPath = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            TaskAttachment attachment = TaskAttachment.builder()
                    .fileName(originalFileName)
                    .filePath(storedFileName)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .task(task)
                    .uploadedBy(user)
                    .build();

            return attachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new FileStorageException("文件上传失败: " + originalFileName, e);
        }
    }

    @Override
    @Transactional
    public void deleteFile(Long attachmentId) {
        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在"));
        deleteFileFromDisk(attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }

    @Override
    @Transactional
    public void deleteAllByTaskId(Long taskId) {
        var attachments = attachmentRepository.findByTaskId(taskId);
        attachments.forEach(a -> deleteFileFromDisk(a.getFilePath()));
        attachmentRepository.deleteByTaskId(taskId);
    }

    private void deleteFileFromDisk(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", fileName);
        }
    }
}
