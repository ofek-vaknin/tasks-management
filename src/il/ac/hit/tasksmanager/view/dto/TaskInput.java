package il.ac.hit.tasksmanager.view.dto;

import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import java.time.LocalDate;

/**
 * Record DTO for dialog input.
 * - Holds validated form data: title, description, state, dueDate, recurrenceDays.
 * - Returned from TaskFormDialog#getTaskInput() when the user presses OK.
 * - Used by the View (MainWindow) in onAdd/onEdit to:
 *   - decide whether to create BasicTask or RecurringTask (by recurrenceDays)
 *   - call the ViewModel (addTask/addRecurringTask or updateTask)
 * This isolates the UI from the domain using an immutable, easy-to-pass object.
 */
public record TaskInput(String title, String description, TaskState state, LocalDate dueDate, int recurrenceDays) {}





