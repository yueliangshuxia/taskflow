package com.taskflow.service.impl;

import com.taskflow.dto.UserProfileDto;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(String username, UserProfileDto profileDto) {
        User user = getCurrentUser(username);

        if (profileDto.getEmail() != null) {
            user.setEmail(profileDto.getEmail());
        }
        if (profileDto.getDisplayName() != null) {
            user.setDisplayName(profileDto.getDisplayName());
        }

        userRepository.save(user);
        return profileDto;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getCurrentUser(username);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateAvatar(String username, String avatarPath) {
        User user = getCurrentUser(username);
        user.setAvatar(avatarPath);
        userRepository.save(user);
    }
}
