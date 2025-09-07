package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.entities.TaskState;
import java.time.LocalDate;

/**
 * RecurringTask represents a task that repeats at a fixed interval of days.
 */
public record RecurringTask(long id, String title, String description, TaskState state, LocalDate dueDate, int recurrenceInterval) implements Task {}


