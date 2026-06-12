package com.taskflow.controller;

import com.taskflow.dto.UserProfileDto;
import com.taskflow.entity.User;
import com.taskflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication auth) {
        User user = userService.getCurrentUser(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("title", "个人资料");
        model.addAttribute("content", "users/profile");
        return "layout/base";
    }

    @PostMapping("/profile")
    public String updateProfile(UserProfileDto profileDto, Model model, Authentication auth) {
        try {
            userService.updateProfile(auth.getName(), profileDto);
            model.addAttribute("message", "资料更新成功");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        User user = userService.getCurrentUser(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("title", "个人资料");
        model.addAttribute("content", "users/profile");
        return "layout/base";
    }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Model model, Authentication auth) {
        try {
            userService.changePassword(auth.getName(), oldPassword, newPassword);
            model.addAttribute("message", "密码修改成功");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        User user = userService.getCurrentUser(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("title", "个人资料");
        model.addAttribute("content", "users/profile");
        return "layout/base";
    }
}
