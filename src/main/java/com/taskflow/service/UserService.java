package com.taskflow.service;

import com.taskflow.dto.UserProfileDto;
import com.taskflow.entity.User;

public interface UserService {
    User getCurrentUser(String username);
    UserProfileDto updateProfile(String username, UserProfileDto profileDto);
    void changePassword(String username, String oldPassword, String newPassword);
    void updateAvatar(String username, String avatarPath);
}
