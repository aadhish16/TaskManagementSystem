# Task Management System - Project Report

## Demonstration and Submission

The application is a Java Swing desktop program and must be demonstrated from a machine with MySQL running. Before the Model Examination, prepare the database with `database.sql`, confirm the credentials in `DatabaseConnection.java`, then launch the application and demonstrate the login, role-specific dashboard, task form, search/filter, and task status update workflows.

A live demonstration cannot be recorded in this report. Capture the actual running application screens and add them under `screenshots/` before final submission. The manual cases below are a demonstration checklist; they are not represented as executed unless a result is recorded.

## 1. System Requirements

### Hardware

The source code does not prescribe or report measured hardware requirements. The following are practical minimum recommendations for a desktop running the application and a local MySQL server:

| Component | Recommended minimum |
|---|---|
| Processor | Dual-core CPU |
| Memory | 4 GB RAM |
| Available storage | 500 MB for the JDK, Maven dependencies, MySQL, and project files |
| Display | 1366 x 768 or higher for the Swing dashboard |
| Network | Not required when MySQL runs locally; internet is needed initially if Maven dependencies must be downloaded |

These are operational recommendations, not benchmarked limits. Running MySQL on another computer requires network access and an updated JDBC URL.

### Software

| Software | Requirement / version used |
|---|---|
| Java Development Kit | JDK 17 or later; Maven compiler release is 17 |
| MySQL Server | MySQL 8.x, using the `task_management` schema |
| Apache Maven | Maven 3.8+ recommended for build and test commands |
| MySQL Connector/J | 8.4.0, declared in `pom.xml` |
| Java GUI | Swing, included with the JDK |
| Test framework | JUnit Jupiter 5.10.2 |
| Operating system | Windows, macOS, or Linux with a compatible JDK and MySQL installation |

## 2. System Design

The application uses a layered desktop architecture. Swing views handle user interaction and validation feedback; DAO interfaces separate those views from database details; DAO implementations execute parameterized JDBC statements; MySQL stores accounts and assigned tasks.

```text
Main
  -> LoginFrame -> UserDAO -> UserDAOImpl
  -> DashboardFrame -> TaskPanel -> TaskDAO -> TaskDAOImpl
                                            -> DatabaseConnection
                                            -> JDBC driver
                                            -> MySQL (task_management)
```

`Main` creates the login screen on Swing's Event Dispatch Thread. After successful authentication, the current `User` is passed to the dashboard and task panel. The DAOs enforce role-based access in their operations: administrators manage task details, while students view their assigned tasks and may update only their status. The database has `users` and `tasks` tables, with `tasks.assigned_user_id` referencing `users.id`.

The GUI calls DAO methods and does not contain SQL. Task rows are mapped from `ResultSet` objects into `Task` model instances. The `TaskPanel` displays these objects in a sortable `JTable`; its controls invoke add, update, delete, search, filter, and refresh operations. Dashboard summary counts are obtained through DAO methods.

## 3. Core Modules Implemented

| Module | Main files | Functionality |
|---|---|---|
| Application entry point | `Main.java` | Configures Swing look and feel and opens the login window on the event dispatch thread. |
| GUI | `LoginFrame.java`, `DashboardFrame.java`, `TaskPanel.java`, `UIStyle.java` | Login, role-specific dashboard, task form/table, search, filters, summary cards, dialogs, and shared styling. |
| Models | `Task.java`, `User.java` | Represent task data and authenticated user identity/role. |
| Task data access | `TaskDAO.java`, `TaskDAOImpl.java` | Define and implement task CRUD, search, filter, assignment, and summary count operations. |
| User data access | `UserDAO.java`, `UserDAOImpl.java` | Authenticate login credentials and retrieve student accounts for assignment. |
| Database connectivity | `DatabaseConnection.java` | Creates a MySQL JDBC connection and provides diagnostic SQL exceptions. |
| Validation | `ValidationUtil.java`, `ValidationException.java` | Check required fields, length limits, date format/date validity, and allowed priority/status values. |
| Database setup | `database.sql` | Creates the schema and tables and inserts sample users and tasks. |
| Automated tests | `ValidationUtilTest.java` | Verifies form input validation without requiring a live database. |

## 4. JDBC Connectivity and Operations

`DatabaseConnection.getConnection()` loads the MySQL Connector/J driver and opens a connection to `jdbc:mysql://localhost:3306/task_management`. Configure `DB_USER` and `DB_PASSWORD` in `DatabaseConnection.java` for the local MySQL installation. The current code stores these settings in source; credentials should not be committed for a real deployment.

DAO methods use `PreparedStatement` parameters for values and try-with-resources to close JDBC resources. SELECT operations call `executeQuery()` and map each result row to a model. INSERT, UPDATE, and DELETE operations call `executeUpdate()`. Task insertion requests generated keys so the newly created task receives its database ID.

Example from `TaskDAOImpl`:

```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
    setTaskParameters(ps, task);
    ps.setInt(6, task.getAssignedUserId());
    int rows = ps.executeUpdate();
    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
            task.setId(keys.getInt(1));
        }
    }
    return rows > 0;
}
```

The principal database operations are:

| Operation | JDBC method | Purpose |
|---|---|---|
| Create | `addTask` | Insert a task with its assignee and retrieve the generated ID. |
| Read | `getAllTasks`, `getTaskById` | Retrieve all visible tasks or one task; student queries are restricted to the current user's assignments. |
| Search | `searchTasks` | Match ID, title, status, or priority using bound parameters and `LIKE`. |
| Filter | `filterTasks` | Apply optional priority/status criteria. |
| Update | `updateTask`, `updateTaskStatus` | Administrators edit task details; students update status only on their own assignments. |
| Delete | `deleteTask` | Delete by task ID; restricted to administrators. |
| Aggregate | `countAllTasks`, `countTasksByStatus` | Supply dashboard summary counts. |
| Authentication | `authenticateUser` | Find a matching username/password and return the user's ID and role. |

## 5. Important Code Snippets

### Start Swing on its event dispatch thread (`Main.java`)

```java
SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
```

### Validate input before creating a task (`ValidationUtil.java`)

```java
requireNotEmpty(title, "Title");
requireMaxLength(title, MAX_TITLE_LENGTH, "Title");
requireNotEmpty(description, "Description");
requireMaxLength(description, MAX_DESCRIPTION_LENGTH, "Description");
LocalDate dueDate = parseDate(dueDateText);
requireSelection(priority, Task.PRIORITIES, "Priority");
requireSelection(status, Task.STATUSES, "Status");

return new Task(title.trim(), description.trim(), dueDate, priority, status);
```

### Restrict student status updates (`TaskDAOImpl.java`)

```java
private static final String UPDATE_STATUS_SQL =
        "UPDATE tasks SET status = ? WHERE id = ? AND assigned_user_id = ?";
```

The DAO also verifies the actor is a student and the requested status is one of the supported values before executing this statement.

## 6. Screenshots

The paths below are intended submission locations. No screenshot files are currently included in the repository; replace each pending item with a screenshot captured from the running application. Remove any screenshot that is not relevant to the demonstrated workflow.

| Evidence | File to add | Status |
|---|---|---|
| Login screen | `screenshots/login.png` | Pending capture |
| Administrator dashboard and task list | `screenshots/admin-dashboard.png` | Pending capture |
| Student dashboard and assigned tasks | `screenshots/student-dashboard.png` | Pending capture |
| Add/update task workflow | `screenshots/task-form.png` | Pending capture |
| Search and filters | `screenshots/search-filter.png` | Pending capture |
| Delete confirmation | `screenshots/delete-confirmation.png` | Pending capture |

After adding the files, embed them here, for example:

```markdown
![Administrator dashboard](screenshots/admin-dashboard.png)
```

## 7. Test Cases and Results

### Automated tests

`ValidationUtilTest` covers valid task construction, empty title/description, invalid calendar dates, incorrect date formats, missing priority/status, and titles over the maximum length. Run with `mvn test`. Record the command's observed pass/fail summary here after execution; these tests do not require MySQL.

### Manual demonstration cases

| ID | Test | Expected result | Result |
|---|---|---|---|
| M01 | Log in as `admin` using the seeded credentials | Administrator dashboard opens and management actions are available. | Pending live demo |
| M02 | Log in as `student` using the seeded credentials | Student sees assigned tasks and status-only editing controls. | Pending live demo |
| M03 | Submit an empty task title or description | Validation message is displayed and no row is inserted. | Pending live demo |
| M04 | Submit an impossible date such as `2026-02-30` | Validation rejects the date and no row is inserted. | Pending live demo |
| M05 | Add a valid task as administrator | Task is stored, appears in the table, and its generated ID is available. | Pending live demo |
| M06 | Search by title, ID, status, or priority | Matching tasks are displayed; input is treated as a parameter, not SQL. | Pending live demo |
| M07 | Filter by priority and status | Only tasks satisfying the selected criteria are displayed. | Pending live demo |
| M08 | Update task details as administrator | Selected task is updated and the dashboard counts refresh. | Pending live demo |
| M09 | Update status as a student | The student's assigned task status changes; other task details remain read-only. | Pending live demo |
| M10 | Attempt task management as a student or status change for another user | DAO role/assignment restrictions prevent unauthorized updates. | Pending live demo |
| M11 | Delete a task and cancel the confirmation | The task remains in the database and table. | Pending live demo |
| M12 | Delete a task and confirm | The task is removed and the table/counts refresh. | Pending live demo |
| M13 | Stop MySQL and attempt database access | A database error is reported to the user. | Pending live demo |

## 8. GitHub Repository Link

[Task Management System source repository](https://github.com/aadhish16/TaskManagementSystem)

## Live Demonstration Checklist

1. Start MySQL and load `database.sql` in a clean local environment.
2. Confirm the JDBC URL and credentials in `DatabaseConnection.java`.
3. Run `mvn test` and record the actual test summary above.
4. Start the application with `mvn clean compile exec:java` or run `com.taskmanagement.Main` from the IDE.
5. Demonstrate administrator login and task CRUD/search/filter operations.
6. Demonstrate student login and the restricted status-update workflow.
7. Capture the screens listed above and add them under `screenshots/`.
8. Confirm the GitHub repository contains the final source, SQL setup script, and this report before submission.
