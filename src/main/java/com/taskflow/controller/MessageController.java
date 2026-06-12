package com.taskflow.controller;

import com.taskflow.dao.MessageRepository;
import com.taskflow.dao.UserRepository;
import com.taskflow.entity.Message;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String inbox(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model, Authentication auth) {
        User user = getUser(auth);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);

        model.addAttribute("messages", messages.getContent());
        model.addAttribute("currentPage", messages.getNumber());
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("unreadCount", messageRepository.countByRecipientIdAndIsRead(user.getId(), false));
        model.addAttribute("activeTab", "inbox");
        model.addAttribute("title", "站内信");
        model.addAttribute("content", "users/messages");
        return "layout/base";
    }

    @GetMapping("/sent")
    public String sent(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model, Authentication auth) {
        User user = getUser(auth);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageRepository.findBySenderIdOrderByCreatedAtDesc(user.getId(), pageable);

        model.addAttribute("messages", messages.getContent());
        model.addAttribute("currentPage", messages.getNumber());
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("unreadCount", messageRepository.countByRecipientIdAndIsRead(user.getId(), false));
        model.addAttribute("activeTab", "sent");
        model.addAttribute("title", "已发送");
        model.addAttribute("content", "users/messages");
        return "layout/base";
    }

    @GetMapping("/compose")
    public String showComposeForm(Model model, Authentication auth) {
        User user = getUser(auth);
        var allUsers = userRepository.findAll();

        model.addAttribute("users", allUsers);
        model.addAttribute("unreadCount", messageRepository.countByRecipientIdAndIsRead(user.getId(), false));
        model.addAttribute("title", "写消息");
        model.addAttribute("content", "users/message-compose");
        return "layout/base";
    }

    @PostMapping("/compose")
    @Transactional
    public String sendMessage(@RequestParam Long recipientId,
                              @RequestParam String subject,
                              @RequestParam String content,
                              Authentication auth,
                              RedirectAttributes redirectAttrs) {
        try {
            User sender = getUser(auth);
            User recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new ResourceNotFoundException("收件人不存在"));

            Message message = Message.builder()
                    .subject(subject)
                    .content(content)
                    .sender(sender)
                    .recipient(recipient)
                    .build();
            messageRepository.save(message);
            redirectAttrs.addFlashAttribute("message", "消息已发送");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "发送失败: " + e.getMessage());
        }
        return "redirect:/messages";
    }

    @GetMapping("/{id}")
    @Transactional
    public String viewMessage(@PathVariable Long id, Model model, Authentication auth) {
        User user = getUser(auth);
        Message message = messageRepository.findWithGraphById(id)
                .orElseThrow(() -> new ResourceNotFoundException("消息不存在"));

        // Mark as read if recipient
        if (message.getRecipient().getId().equals(user.getId()) && !message.isRead()) {
            messageRepository.markAsRead(id, user.getId());
            message.setRead(true);
        }

        model.addAttribute("msg", message);
        model.addAttribute("unreadCount", messageRepository.countByRecipientIdAndIsRead(user.getId(), false));
        model.addAttribute("title", "消息 - " + message.getSubject());
        model.addAttribute("content", "users/message-detail");
        return "layout/base";
    }

    private User getUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }
}
