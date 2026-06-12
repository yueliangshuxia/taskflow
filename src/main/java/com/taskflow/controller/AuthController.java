package com.taskflow.controller;

import com.taskflow.dto.UserRegistrationDto;
import com.taskflow.entity.User;
import com.taskflow.entity.enums.Role;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String expired,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        if (logout != null) {
            model.addAttribute("message", "您已成功退出登录");
        }
        if (expired != null) {
            model.addAttribute("error", "会话已过期，请重新登录");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid UserRegistrationDto registrationDto,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", registrationDto);
            return "auth/register";
        }

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "两次密码输入不一致");
            model.addAttribute("user", registrationDto);
            return "auth/register";
        }

        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            result.rejectValue("username", "error.user", "用户名已存在");
            model.addAttribute("user", registrationDto);
            return "auth/register";
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            result.rejectValue("email", "error.user", "邮箱已被注册");
            model.addAttribute("user", registrationDto);
            return "auth/register";
        }

        User user = User.builder()
                .username(registrationDto.getUsername())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .email(registrationDto.getEmail())
                .displayName(registrationDto.getDisplayName() != null ?
                        registrationDto.getDisplayName() : registrationDto.getUsername())
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);

        auditLogService.log("REGISTER", "User", user.getId(),
                "用户注册: " + user.getUsername(), user.getUsername());

        return "redirect:/login?registered=true";
    }
}
