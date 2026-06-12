package com.taskflow.config;

import com.taskflow.entity.User;
import com.taskflow.entity.enums.Role;
import com.taskflow.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("No users found, creating default admin account...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@taskflow.com")
                    .displayName("系统管理员")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);

            User demoUser = User.builder()
                    .username("demo")
                    .password(passwordEncoder.encode("demo123"))
                    .email("demo@taskflow.com")
                    .displayName("演示用户")
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            userRepository.save(demoUser);

            log.info("Default accounts created: admin/admin123, demo/demo123");
        }
    }
}
