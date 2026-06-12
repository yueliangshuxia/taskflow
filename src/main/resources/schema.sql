-- ============================================================
-- TaskFlow Database Schema
-- Run this script to initialize the database:
--   mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS taskflow
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE taskflow;

-- Spring Security Remember-Me persistent logins table
CREATE TABLE IF NOT EXISTS persistent_logins (
    username  VARCHAR(64) NOT NULL,
    series    VARCHAR(64) PRIMARY KEY,
    token     VARCHAR(64) NOT NULL,
    last_used TIMESTAMP   NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
