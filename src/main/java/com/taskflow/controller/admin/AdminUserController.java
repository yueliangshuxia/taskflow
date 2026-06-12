package com.taskflow.controller.admin;

import com.taskflow.entity.User;
import com.taskflow.entity.enums.Role;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Page<User> userPage;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (search != null && !search.isEmpty()) {
            userPage = userRepository.findByUsernameContaining(search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("title", "用户管理");
        model.addAttribute("content", "admin/users");
        return "layout/base";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("title", "创建用户");
        model.addAttribute("content", "admin/user-form");
        return "layout/base";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttrs) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRole() == null) user.setRole(Role.USER);
            user.setEnabled(true);
            userRepository.save(user);
            auditLogService.log("CREATE", "User", user.getId(),
                    "管理员创建用户: " + user.getUsername(), "admin");
            redirectAttrs.addFlashAttribute("message", "用户创建成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        model.addAttribute("user", user);
        model.addAttribute("title", "编辑用户");
        model.addAttribute("content", "admin/user-form");
        return "layout/base";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute User formData,
                             RedirectAttributes redirectAttrs) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setEmail(formData.getEmail());
            user.setDisplayName(formData.getDisplayName());
            if (formData.getPassword() != null && !formData.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(formData.getPassword()));
            }
            userRepository.save(user);
            redirectAttrs.addFlashAttribute("message", "用户更新成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            String username = userRepository.findById(id)
                    .map(User::getUsername).orElse("(unknown)");
            userRepository.deleteById(id);
            auditLogService.log("DELETE", "User", id,
                    "管理员删除用户: " + username, "admin");
            redirectAttrs.addFlashAttribute("message", "用户已删除");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role,
                             RedirectAttributes redirectAttrs) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Role oldRole = user.getRole();
            user.setRole(role);
            userRepository.save(user);
            auditLogService.log("CHANGE_ROLE", "User", id,
                    "用户 " + user.getUsername() + " 角色变更: " + oldRole + " → " + role, "admin");
            redirectAttrs.addFlashAttribute("message", "角色更新成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "角色更新失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
