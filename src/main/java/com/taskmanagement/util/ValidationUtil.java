package com.taskmanagement.util;

import com.taskmanagement.model.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;

/**
 * Input validation helpers used by the GUI.
 * All methods are static, so no object needs to be created.
 */
public final class ValidationUtil {

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    // STRICT resolving rejects impossible dates such as 2026-02-30
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private ValidationUtil() {
    }

    /** Returns true if the text is null or contains only spaces. */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Throws a ValidationException if the field is empty. */
    public static void requireNotEmpty(String value, String fieldName) throws ValidationException {
        if (isEmpty(value)) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    /** Throws a ValidationException if the text is longer than maxLength. */
    public static void requireMaxLength(String value, int maxLength, String fieldName) throws ValidationException {
        if (value != null && value.trim().length() > maxLength) {
            throw new ValidationException(fieldName + " cannot be longer than " + maxLength + " characters.");
        }
    }

    /** Parses a date in yyyy-MM-dd format. */
    public static LocalDate parseDate(String text) throws ValidationException {
        requireNotEmpty(text, "Due Date");
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid due date: '" + text.trim()
                    + "'.\nPlease enter a valid date in the format " + DATE_PATTERN
                    + " (for example 2026-12-31).", e);
        }
    }

    /** Checks that a value from a combo box is one of the allowed options. */
    public static void requireSelection(String selected, String[] allowedValues, String fieldName)
            throws ValidationException {
        if (selected == null || !Arrays.asList(allowedValues).contains(selected)) {
            throw new ValidationException("Please select a " + fieldName + ".");
        }
    }

    /**
     * Validates all form values and builds a Task object.
     * Used by both "Add" and "Update", so the rules are written only once.
     */
    public static Task buildTask(String title, String description, String dueDateText,
                                 String priority, String status) throws ValidationException {
        requireNotEmpty(title, "Title");
        requireMaxLength(title, MAX_TITLE_LENGTH, "Title");
        requireNotEmpty(description, "Description");
        requireMaxLength(description, MAX_DESCRIPTION_LENGTH, "Description");
        LocalDate dueDate = parseDate(dueDateText);
        requireSelection(priority, Task.PRIORITIES, "Priority");
        requireSelection(status, Task.STATUSES, "Status");

        return new Task(title.trim(), description.trim(), dueDate, priority, status);
    }
}
