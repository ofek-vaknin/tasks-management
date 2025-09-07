package il.ac.hit.tasksmanager.model.combinator;

import il.ac.hit.tasksmanager.model.Task;
import java.time.LocalDate;

/**
 * Functional combinator of task predicates used for flexible filtering.
 */
@FunctionalInterface
public interface TaskFilter {
	/** Returns true if the given task matches this filter. */
	boolean matches(Task task);

	/** Logical AND of two filters. */
	default TaskFilter and(TaskFilter other) {
		return task -> this.matches(task) && other.matches(task);
	}

	/** Logical OR of two filters. */
	default TaskFilter or(TaskFilter other) {
		return task -> this.matches(task) || other.matches(task);
	}

	/** Matches tasks whose title contains the given text (case-insensitive). */
	static TaskFilter byTitle(String title) {
		return task -> task.title() != null && task.title().toLowerCase().contains(title.toLowerCase());
	}

	/** Matches tasks by state name. */
	static TaskFilter byState(String stateName) {
		return task -> task.state() != null && task.state().name().equalsIgnoreCase(stateName);
	}

	/** Matches tasks with the due date equal to the given date. */
	static TaskFilter byDueDate(LocalDate date) {
		return task -> task.dueDate() != null && task.dueDate().equals(date);
	}
}


