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
    status      VARCHAR(20),
    assigned_user_id INT NOT NULL,
    CONSTRAINT fk_tasks_assigned_user
        FOREIGN KEY (assigned_user_id) REFERENCES users(id)
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
SET @student_id = (SELECT id FROM users WHERE username = 'student');
INSERT INTO tasks (title, description, due_date, priority, status, assigned_user_id) VALUES
    ('Learn Java Swing',       'Study JFrame, JPanel, layouts and event handling',          '2026-10-05', 'HIGH',   'IN PROGRESS', @student_id),
    ('Complete JDBC module',   'Practice PreparedStatement and try-with-resources',          '2026-10-10', 'HIGH',   'PENDING',     @student_id),
    ('DBMS assignment',        'Normalize the library database up to 3NF',                  '2026-10-12', 'MEDIUM', 'PENDING',     @student_id),
    ('Java OOP revision',      'Revise encapsulation, inheritance, polymorphism, abstraction','2026-10-02', 'MEDIUM', 'COMPLETED',   @student_id),
    ('Prepare project report', 'Write the report and take screenshots of the application',  '2026-10-20', 'LOW',    'PENDING',     @student_id),
    ('Team meeting',           'Discuss module split with project teammates',                '2026-09-30', 'LOW',    'COMPLETED',   @student_id);

-- Quick check
SELECT * FROM tasks;
