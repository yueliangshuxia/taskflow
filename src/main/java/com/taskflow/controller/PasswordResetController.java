package com.taskflow.controller;

import com.taskflow.dao.PasswordResetTokenRepository;
import com.taskflow.dao.UserRepository;
import com.taskflow.entity.PasswordResetToken;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    @Transactional
    public String processForgotPassword(@RequestParam String email,
                                         RedirectAttributes redirectAttrs) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            // Remove old tokens
            tokenRepository.deleteByUserId(user.getId());

            // Create new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .build();
            tokenRepository.save(resetToken);

            // In dev mode, log the reset link
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            log.info("Password reset link for {}: {}", email, resetLink);
        }
        // Always show success to prevent email enumeration
        redirectAttrs.addFlashAttribute("message",
                "如果该邮箱已注册，您将收到一封包含重置链接的邮件。\n开发模式下请查看应用日志。");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam String token, Model model) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null || resetToken.isUsed() || resetToken.isExpired()) {
            model.addAttribute("error", "重置链接无效或已过期");
            return "auth/reset-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    @Transactional
    public String processResetPassword(@RequestParam String token,
                                        @RequestParam String password,
                                        @RequestParam String confirmPassword,
                                        RedirectAttributes redirectAttrs) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null || resetToken.isUsed() || resetToken.isExpired()) {
            redirectAttrs.addFlashAttribute("error", "重置链接无效或已过期");
            return "redirect:/login";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("error", "两次密码输入不一致");
            return "redirect:/reset-password?token=" + token;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        redirectAttrs.addFlashAttribute("message", "密码重置成功，请登录");
        return "redirect:/login";
    }
}
