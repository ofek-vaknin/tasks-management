package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import java.time.LocalDate;

/**
 * Represents a domain task within the model layer. This sealed interface
 * is implemented by concrete record types such as {@link BasicTask}
 * and {@link RecurringTask}.
 */
public sealed interface Task permits BasicTask, RecurringTask {
	/**
	 * Returns the unique identifier of the task.
	 * @return the task id
	 */
	int id();

	/**
	 * Returns the task title.
	 * @return the title (non-null)
	 */
	String title();

	/**
	 * Returns the task description, or null if absent.
	 * @return the description or null
	 */
	String description();

	/**
	 * Returns the current state of the task.
	 * @return the {@link TaskState}
	 */
	TaskState state();

	/**
	 * Returns the due date, or null if not set.
	 * @return due date or null
	 */
	LocalDate dueDate();
}


