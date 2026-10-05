# Task Management System

A standalone **Java Swing desktop application** for managing tasks, built with **JDBC**, **MySQL**, **Maven** and the **DAO design pattern**.
It is a college academic project that demonstrates Core Java, OOP, Swing GUI, event handling, input validation, exception handling, JDBC and CRUD operations.

> No web framework, no Spring Boot, no Hibernate/JPA/ORM — plain Java + Swing + JDBC.

---

## 1. Features

- **Login screen** with validation (credentials checked against the `users` table)
- **Dashboard** with summary cards: Total, Pending, In Progress, Completed
- **Add Task** – title, description, due date, priority, status (with validation)
- **View Tasks** – all tasks in a `JTable` with scrolling, sorting and row selection
- **Select a row** – its data is loaded into the form for editing
- **Search** – by task ID, title, status or priority (uses `PreparedStatement` + `LIKE`)
- **Update Task** – edit the selected task and save
- **Delete Task** – with a *"Are you sure you want to delete this task?"* YES/NO confirmation
- **Filter** – by Priority (ALL / LOW / MEDIUM / HIGH) and Status (ALL / PENDING / IN PROGRESS / COMPLETED)
- **Refresh** and **Logout**
- Meaningful error messages with `JOptionPane` for every failure

## 2. Technologies Used

| Technology | Purpose |
|---|---|
| Java 17+ | Core language |
| Java Swing | Desktop GUI |
| JDBC | Database access |
| MySQL 8 | Database |
| MySQL Connector/J 8.4.0 | JDBC driver |
| Maven | Build & dependency management |
| JUnit 5 | Unit tests for validation rules |
| IntelliJ IDEA | IDE (any Maven-aware IDE works) |

## 3. Requirements

- JDK **17 or later** (`java -version`)
- **MySQL Server 8.x** running on `localhost:3306`
- **Maven 3.8+** (or use the Maven bundled with IntelliJ IDEA)

## 4. Default Login Credentials

| Username | Password |
|---|---|
| `admin` | `admin123` |
| `student` | `student123` |

## 5. Database Setup

1. Start MySQL.
2. Run the script (it creates the database, both tables, the login users and sample tasks):

```bash
mysql -u root -p < database.sql
```

Or open `database.sql` in **MySQL Workbench** and click *Execute* (⚡).

Tables created:

```sql
tasks(id INT PK AUTO_INCREMENT, title VARCHAR(100) NOT NULL, description VARCHAR(500),
      due_date DATE, priority VARCHAR(20), status VARCHAR(20))
users(id INT PK AUTO_INCREMENT, username VARCHAR(50) UNIQUE, password VARCHAR(100))
```

## 6. Configure Database Credentials

Open `src/main/java/com/taskmanagement/database/DatabaseConnection.java` and change:

```java
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root";   // <-- your MySQL password
```

The URL is `jdbc:mysql://localhost:3306/task_management` — change the host/port if your MySQL runs elsewhere.

## 7. How to Run

### Option A – IntelliJ IDEA
1. **File → Open** → select the `TaskManagementSystem` folder (the one with `pom.xml`).
2. Wait for Maven to download dependencies.
3. Open `com.taskmanagement.Main` → click the green ▶ Run button.

### Option B – Command line
```bash
mvn clean compile exec:java
```

### Option C – Runnable JAR
```bash
mvn clean package
java -jar target/TaskManagementSystem.jar
```

### Run the unit tests (no database needed)
```bash
mvn test
```

## 8. Project Structure

```
TaskManagementSystem/
├── src/
│   ├── main/
│   │   ├── java/com/taskmanagement/
│   │   │   ├── Main.java                     # Entry point
│   │   │   ├── model/
│   │   │   │   └── Task.java                 # Task entity (encapsulation)
│   │   │   ├── dao/
│   │   │   │   ├── TaskDAO.java              # DAO interface (abstraction)
│   │   │   │   ├── TaskDAOImpl.java          # JDBC implementation (all task SQL)
│   │   │   │   ├── UserDAO.java              # Login DAO interface
│   │   │   │   └── UserDAOImpl.java          # Login JDBC implementation
│   │   │   ├── database/
│   │   │   │   └── DatabaseConnection.java   # URL, user, password, getConnection()
│   │   │   ├── gui/
│   │   │   │   ├── LoginFrame.java           # Login window (extends JFrame)
│   │   │   │   ├── DashboardFrame.java       # Main window + summary cards
│   │   │   │   ├── TaskPanel.java            # Form, table, search, filter (extends JPanel)
│   │   │   │   └── UIStyle.java              # Shared colors, fonts, button factory
│   │   │   └── util/
│   │   │       ├── ValidationUtil.java       # Input validation rules
│   │   │       └── ValidationException.java  # Custom checked exception
│   │   └── resources/
│   └── test/java/com/taskmanagement/util/
│       └── ValidationUtilTest.java           # JUnit tests for validation
├── database.sql
├── pom.xml
├── README.md
└── .gitignore
```

## 9. Architecture

```
GUI  (LoginFrame, DashboardFrame, TaskPanel)      – no SQL here
 ↓   calls methods on the TaskDAO / UserDAO interfaces
DAO  (TaskDAOImpl, UserDAOImpl)                    – all SQL lives here
 ↓   DatabaseConnection.getConnection()
JDBC (PreparedStatement, ResultSet)
 ↓
MySQL (task_management database)
```

## 10. Explanation of Each Class

| Class | Responsibility |
|---|---|
| `Main` | Sets the look and feel and opens `LoginFrame` on the Swing Event Dispatch Thread. |
| `Task` | Model/entity with private fields, 3 constructors, getters/setters, `toString()`. Also holds the allowed priority/status values. |
| `DatabaseConnection` | Stores URL/user/password and returns a new `Connection`. Converts driver/connection problems into clear `SQLException` messages. |
| `TaskDAO` | Interface listing all task operations: `addTask`, `getAllTasks`, `getTaskById`, `searchTasks`, `updateTask`, `deleteTask`, `filterTasks`, plus two count methods for the dashboard. |
| `TaskDAOImpl` | Implements `TaskDAO` with JDBC. Uses `PreparedStatement` for every query and try-with-resources to close everything. Helper methods (`setTaskParameters`, `readTasks`, `mapRow`) avoid duplicate code. |
| `UserDAO` / `UserDAOImpl` | Checks login credentials against the `users` table. |
| `LoginFrame` | Login window (`extends JFrame`) with username, password, Login and Clear buttons, and validation. Enter key triggers Login. |
| `DashboardFrame` | Main window (`extends JFrame`): header with Logout, sidebar menu (Add, View, Search, Update, Delete, Refresh), four summary cards and the `TaskPanel`. Contains the inner class `SummaryCard extends JPanel`. |
| `TaskPanel` | `extends JPanel`: task form, `JTable`, search box and filters. Handles all CRUD button events by calling the DAO. |
| `UIStyle` | Shared colors/fonts and `createButton()` so styling is not repeated. |
| `ValidationUtil` | Static validation helpers; `buildTask()` validates the whole form and returns a `Task`, used by both Add and Update. |
| `ValidationException` | Custom checked exception (`extends Exception`) for invalid input. |

## 11. OOP Concepts Used

| Concept | Where |
|---|---|
| **Encapsulation** | `Task` has `private` fields accessed only via getters/setters. `DatabaseConnection` hides credentials in `private static final` fields. GUI fields are `private`. |
| **Inheritance** | `LoginFrame` and `DashboardFrame` extend `JFrame`; `TaskPanel` and `SummaryCard` extend `JPanel`; `ValidationException` extends `Exception`; anonymous `DefaultTableModel` subclass in `TaskPanel`. |
| **Polymorphism** | `TaskDAO taskDAO = new TaskDAOImpl();` – interface reference, implementation object (runtime polymorphism). Overridden methods: `toString()`, `isCellEditable()`, `getColumnClass()`, all `@Override` DAO methods. Constructor overloading in `Task` (compile-time polymorphism). |
| **Abstraction** | `TaskDAO` and `UserDAO` interfaces expose *what* can be done; the GUI does not know *how* (SQL/JDBC) it is done. |

## 12. JDBC Operations (CRUD)

| Operation | Method | SQL |
|---|---|---|
| **C**reate | `addTask()` | `INSERT INTO tasks (title, description, due_date, priority, status) VALUES (?, ?, ?, ?, ?)` |
| **R**ead | `getAllTasks()` | `SELECT ... FROM tasks ORDER BY id` |
| **R**ead | `getTaskById()` | `SELECT ... FROM tasks WHERE id = ?` |
| **R**ead | `searchTasks()` | `SELECT ... WHERE id = ? OR title LIKE ? OR status LIKE ? OR priority LIKE ?` |
| **R**ead | `filterTasks()` | `SELECT ... WHERE 1 = 1 [AND priority = ?] [AND status = ?]` |
| **U**pdate | `updateTask()` | `UPDATE tasks SET title = ?, description = ?, due_date = ?, priority = ?, status = ? WHERE id = ?` |
| **D**elete | `deleteTask()` | `DELETE FROM tasks WHERE id = ?` |

JDBC steps used in every method:
1. `DatabaseConnection.getConnection()` opens a `Connection`.
2. `conn.prepareStatement(sql)` creates a `PreparedStatement`.
3. Values are bound with `setString`, `setInt`, `setDate` (`?` placeholders → **no SQL injection**).
4. `executeQuery()` (SELECT) or `executeUpdate()` (INSERT/UPDATE/DELETE).
5. `ResultSet` rows are converted to `Task` objects in `mapRow()`.
6. **try-with-resources** automatically closes `ResultSet`, `PreparedStatement` and `Connection`.
7. `SQLException` is passed up to the GUI, which shows a `JOptionPane` error.

`addTask()` also uses `Statement.RETURN_GENERATED_KEYS` to read the new auto-increment `id`.

## 13. DAO Design Pattern

The **Data Access Object** pattern separates *business/GUI code* from *database code*.

- **Model** – `Task` (the data being stored)
- **DAO interface** – `TaskDAO` (the contract: which operations exist)
- **DAO implementation** – `TaskDAOImpl` (the JDBC/SQL code)
- **Client** – `TaskPanel` / `DashboardFrame` (use the interface only)

Benefits:
- The GUI contains **no SQL**.
- Database code is in one place, easy to test and maintain.
- The database could be switched (e.g. to PostgreSQL) by writing a new `TaskDAO` implementation without touching the GUI.

## 14. Exception Handling

| Situation | How it is handled |
|---|---|
| MySQL not running / wrong password | `DatabaseConnection` throws a `SQLException` with a clear message → "Database Error" dialog |
| JDBC driver missing | `ClassNotFoundException` wrapped in `SQLException` with a helpful message |
| Empty title/description | `ValidationException` → "Title cannot be empty." |
| Invalid date (e.g. `2026-02-30`, `31/12/2026`) | `DateTimeParseException` wrapped in `ValidationException` |
| Priority/status not selected | `ValidationException` → "Please select a Priority." |
| Update/Delete without selecting a row | Warning: "Please select a task from the table first." |
| Task deleted by someone else | DAO returns `false`/`null` → error message and list refresh |
| Any SQL error | Caught in the GUI, shown in a `JOptionPane` |

No catch block is empty.

## 15. Sample Test Cases

### Manual test cases

| # | Test | Steps | Expected Result |
|---|---|---|---|
| TC01 | Valid login | Username `admin`, password `admin123`, click Login | Dashboard opens |
| TC02 | Wrong password | `admin` / `wrong` | "Invalid username or password." |
| TC03 | Empty login fields | Click Login with empty fields | "Username cannot be empty." |
| TC04 | Clear login | Type text, click Clear | Both fields emptied |
| TC05 | Database down | Stop MySQL, try to log in | "Unable to connect to the database..." |
| TC06 | Add valid task | Title `Java Project`, description, `2026-12-15`, HIGH, PENDING → Add Task | "Task added successfully", row appears, Total/Pending cards increase |
| TC07 | Empty title | Leave title blank → Add Task | "Title cannot be empty." |
| TC08 | Empty description | Leave description blank → Add Task | "Description cannot be empty." |
| TC09 | Invalid date | Due date `2026-02-30` → Add Task | "Invalid due date..." |
| TC10 | Wrong date format | Due date `15/12/2026` → Add Task | "Invalid due date..." |
| TC11 | No priority | Priority `-- Select --` → Add Task | "Please select a Priority." |
| TC12 | No status | Status `-- Select --` → Add Task | "Please select a Status." |
| TC13 | Select row | Click a row in the table | Form fills with that task, "Selected Task ID: n" |
| TC14 | Update task | Select row, change status to COMPLETED → Update | "Task ID n updated successfully", cards update |
| TC15 | Update without selection | Clear form → Update | "Please select a task from the table first." |
| TC16 | Delete – NO | Select row → Delete → click No | Task is **not** deleted |
| TC17 | Delete – YES | Select row → Delete → click Yes | "Task ID n deleted successfully", row removed |
| TC18 | Search by title | Search `Java` | Only tasks with "Java" in the title |
| TC19 | Search by ID | Search `2` | Task with ID 2 (plus any whose text contains "2") |
| TC20 | Search by status | Search `pending` | All PENDING tasks |
| TC21 | Empty search | Click Search with empty box | "Please enter an ID, title, status or priority..." |
| TC22 | SQL injection attempt | Search `' OR '1'='1` | No error, no unexpected results (treated as plain text) |
| TC23 | Filter | Priority HIGH, Status PENDING → Filter | Only high-priority pending tasks |
| TC24 | Filter ALL/ALL | Both ALL → Filter | All tasks |
| TC25 | Refresh | Click Refresh | Search/filters reset, all tasks shown |
| TC26 | Logout | Logout → Yes | Returns to login screen |

### Automated tests
`ValidationUtilTest` (JUnit 5) checks valid input, empty title/description, impossible dates, wrong date format, missing priority/status and title length. Run with `mvn test`.

## 16. Screenshots

Add your screenshots to a `screenshots/` folder and reference them here:

| Screen | Image |
|---|---|
| Login | `![Login](screenshots/login.png)` |
| Dashboard | `![Dashboard](screenshots/dashboard.png)` |
| Add Task | `![Add Task](screenshots/add-task.png)` |
| Search / Filter | `![Search](screenshots/search.png)` |
| Delete confirmation | `![Delete](screenshots/delete.png)` |

## 17. Future Enhancements

- Store passwords as salted hashes (e.g. BCrypt) instead of plain text
- User registration and per-user tasks
- Date picker component instead of typing the date
- Highlight overdue tasks in red
- Export tasks to CSV/PDF
- Email/desktop reminders before the due date
- Connection pooling (e.g. HikariCP) and a `db.properties` config file
- Dark mode theme

## 18. Troubleshooting

| Problem | Fix |
|---|---|
| `Unable to connect to the database` | Start MySQL; check `DB_USER` / `DB_PASSWORD` in `DatabaseConnection.java`; make sure `database.sql` was run. |
| `Unknown database 'task_management'` | Run `database.sql`. |
| `Public Key Retrieval is not allowed` | Already handled by `allowPublicKeyRetrieval=true` in the URL. |
| `MySQL JDBC driver not found` | Run through Maven/IntelliJ so `mysql-connector-j` is on the classpath. |
