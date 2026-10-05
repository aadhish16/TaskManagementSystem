package com.taskmanagement.util;

import com.taskmanagement.model.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the validation rules (they do not need MySQL).
 * Run with: mvn test
 */
class ValidationUtilTest {

    @Test
    void validInputBuildsTask() throws ValidationException {
        Task task = ValidationUtil.buildTask("  Learn JDBC ", "Practice CRUD", "2026-12-31", "HIGH", "PENDING");
        assertEquals("Learn JDBC", task.getTitle());
        assertEquals(LocalDate.of(2026, 12, 31), task.getDueDate());
        assertEquals("HIGH", task.getPriority());
        assertEquals("PENDING", task.getStatus());
    }

    @Test
    void emptyTitleIsRejected() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidationUtil.buildTask("  ", "desc", "2026-12-31", "LOW", "PENDING"));
        assertEquals("Title cannot be empty.", ex.getMessage());
    }

    @Test
    void emptyDescriptionIsRejected() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.buildTask("Title", "", "2026-12-31", "LOW", "PENDING"));
    }

    @Test
    void impossibleDateIsRejected() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidationUtil.buildTask("Title", "desc", "2026-02-30", "LOW", "PENDING"));
        assertTrue(ex.getMessage().startsWith("Invalid due date"));
    }

    @Test
    void wrongDateFormatIsRejected() {
        assertThrows(ValidationException.class, () -> ValidationUtil.parseDate("31/12/2026"));
    }

    @Test
    void placeholderPriorityIsRejected() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidationUtil.buildTask("Title", "desc", "2026-12-31", "-- Select --", "PENDING"));
        assertEquals("Please select a Priority.", ex.getMessage());
    }

    @Test
    void placeholderStatusIsRejected() {
        assertThrows(ValidationException.class,
                () -> ValidationUtil.buildTask("Title", "desc", "2026-12-31", "LOW", null));
    }

    @Test
    void tooLongTitleIsRejected() {
        String longTitle = "a".repeat(101);
        assertThrows(ValidationException.class,
                () -> ValidationUtil.buildTask(longTitle, "desc", "2026-12-31", "LOW", "PENDING"));
    }
}
