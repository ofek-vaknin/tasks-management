package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.entities.TaskState;
import java.time.LocalDate;

/**
 * Task is the public model interface for tasks. Implementations are records (e.g., BasicTask, RecurringTask).
 */
public sealed interface Task permits BasicTask, RecurringTask {
	/** Returns the unique identifier of the task. */
	long id();
	/** Returns the task title. */
	String title();
	/** Returns the task description, or null if absent. */
	String description();
	/** Returns the current state of the task. */
	TaskState state();
	/** Returns the due date, or null if not set. */
	LocalDate dueDate();
}


