-- =====================================================
--  Task Management System - Database Script (MySQL 8+)
-- =====================================================

CREATE DATABASE IF NOT EXISTS task_management;
USE task_management;

-- Drop old tables so the script can be re-run safely
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS users;

-- -----------------------------------------------------
-- Table: users (used by the login screen)
-- -----------------------------------------------------
CREATE TABLE users (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'
);

-- -----------------------------------------------------
-- Table: tasks
-- -----------------------------------------------------
CREATE TABLE tasks (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    due_date    DATE,
    priority    VARCHAR(20),
    status      VARCHAR(20)
);

-- -----------------------------------------------------
-- Default login (username: admin, password: admin123)
-- -----------------------------------------------------
INSERT INTO users (username, password, role) VALUES
    ('admin',   'admin123',   'ADMIN'),
    ('student', 'student123', 'STUDENT');

-- -----------------------------------------------------
-- Sample tasks
-- -----------------------------------------------------
INSERT INTO tasks (title, description, due_date, priority, status) VALUES
    ('Learn Java Swing',       'Study JFrame, JPanel, layouts and event handling',          '2026-10-05', 'HIGH',   'IN PROGRESS'),
    ('Complete JDBC module',   'Practice PreparedStatement and try-with-resources',          '2026-10-10', 'HIGH',   'PENDING'),
    ('DBMS assignment',        'Normalize the library database up to 3NF',                  '2026-10-12', 'MEDIUM', 'PENDING'),
    ('Java OOP revision',      'Revise encapsulation, inheritance, polymorphism, abstraction','2026-10-02', 'MEDIUM', 'COMPLETED'),
    ('Prepare project report', 'Write the report and take screenshots of the application',  '2026-10-20', 'LOW',    'PENDING'),
    ('Team meeting',           'Discuss module split with project teammates',                '2026-09-30', 'LOW',    'COMPLETED');

-- Quick check
SELECT * FROM tasks;
