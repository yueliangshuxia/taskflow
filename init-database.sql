-- =============================================================
-- TaskFlow 项目管理系统 - 数据库初始化脚本
-- 使用方法：
--   方式一（命令行）：
--     mysql -u root -p < init-database.sql
--
--   方式二（MySQL Workbench）：
--     文件 → 打开 SQL 脚本 → 选择本文件 → 执行
--
--   方式三（登录后执行）：
--     source init-database.sql;
--
-- 注意：如果已有 taskflow 数据库，会先删除重建
-- =============================================================

-- 删除已有数据库（如有）
DROP DATABASE IF EXISTS `taskflow`;

-- 创建数据库
CREATE DATABASE `taskflow` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `taskflow`;

-- ======================== 表结构 ========================

-- 1. 用户表
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT 'BCrypt加密密码',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `display_name` varchar(100) DEFAULT NULL COMMENT '显示名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像路径',
  `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_username` (`username`),
  UNIQUE KEY `UK_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 项目表
CREATE TABLE `projects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `description` text COMMENT '项目描述',
  `created_at` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `owner_id` bigint NOT NULL COMMENT '项目所有者ID',
  PRIMARY KEY (`id`),
  KEY `FK_owner` (`owner_id`),
  CONSTRAINT `FK_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 项目成员表（多对多）
CREATE TABLE `project_members` (
  `project_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  KEY `FK_member_user` (`user_id`),
  KEY `FK_member_project` (`project_id`),
  CONSTRAINT `FK_member_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `FK_member_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 任务表
CREATE TABLE `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '任务标题',
  `description` text COMMENT '任务描述',
  `status` varchar(20) NOT NULL DEFAULT 'TODO' COMMENT '状态: TODO/IN_PROGRESS/DONE',
  `priority` varchar(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级: LOW/MEDIUM/HIGH/URGENT',
  `due_date` date DEFAULT NULL COMMENT '截止日期',
  `created_at` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `project_id` bigint DEFAULT NULL COMMENT '所属项目ID',
  `assignee_id` bigint DEFAULT NULL COMMENT '指派人ID',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  PRIMARY KEY (`id`),
  KEY `FK_task_project` (`project_id`),
  KEY `FK_task_assignee` (`assignee_id`),
  KEY `FK_task_creator` (`creator_id`),
  CONSTRAINT `FK_task_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `FK_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK_task_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 评论表
CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL COMMENT '评论内容',
  `created_at` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `task_id` bigint NOT NULL COMMENT '所属任务ID',
  `author_id` bigint NOT NULL COMMENT '作者ID',
  PRIMARY KEY (`id`),
  KEY `FK_comment_task` (`task_id`),
  KEY `FK_comment_author` (`author_id`),
  CONSTRAINT `FK_comment_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FK_comment_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 任务附件表
CREATE TABLE `task_attachments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL COMMENT '原文件名',
  `file_path` varchar(500) NOT NULL COMMENT '存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `content_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
  `uploaded_at` datetime(6) DEFAULT NULL COMMENT '上传时间',
  `task_id` bigint NOT NULL COMMENT '所属任务ID',
  `uploaded_by` bigint NOT NULL COMMENT '上传者ID',
  PRIMARY KEY (`id`),
  KEY `FK_attachment_task` (`task_id`),
  KEY `FK_attachment_uploader` (`uploaded_by`),
  CONSTRAINT `FK_attachment_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FK_attachment_uploader` FOREIGN KEY (`uploaded_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 页面访问日志表
CREATE TABLE `visit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `page_url` varchar(500) DEFAULT NULL COMMENT '访问URL',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `username` varchar(50) DEFAULT NULL COMMENT '访问用户',
  `visited_at` datetime(6) DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 操作审计日志表
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(100) NOT NULL COMMENT '操作名称',
  `entity_type` varchar(50) DEFAULT NULL COMMENT '对象类型',
  `entity_id` bigint DEFAULT NULL COMMENT '对象ID',
  `details` text COMMENT '操作详情',
  `performed_by` varchar(50) DEFAULT NULL COMMENT '操作者',
  `performed_at` datetime(6) DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Remember-Me 持久登录表（Spring Security 需要）
CREATE TABLE `persistent_logins` (
  `username` varchar(64) NOT NULL,
  `series` varchar(64) NOT NULL,
  `token` varchar(64) NOT NULL,
  `last_used` timestamp NOT NULL,
  PRIMARY KEY (`series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ======================== 种子数据 ========================

-- 插入默认用户（密码通过 BCrypt 加密）
-- admin / admin123（管理员角色）
-- demo  / demo123（普通用户角色）
INSERT INTO `users` (`id`, `username`, `password`, `email`, `display_name`, `role`, `enabled`, `created_at`, `updated_at`)
VALUES
  (1, 'admin', '$2a$10$45aiWMam/Zi/e0.IRx8dA.m79Dwidl4111KIzk9zlX8zXSi8xT5yu', 'admin@taskflow.com', '系统管理员', 'ADMIN', 1, NOW(), NOW()),
  (2, 'demo', '$2a$10$TdHPEWufURTddTU/r7OYM.DkMF/WeQkmiuZbnIAgrXVeewg0H/oaC', 'demo@taskflow.com', '演示用户', 'USER', 1, NOW(), NOW());
