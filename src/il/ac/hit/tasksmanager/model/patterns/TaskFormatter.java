package il.ac.hit.tasksmanager.model.patterns;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.RecurringTask;
import il.ac.hit.tasksmanager.model.entities.TaskState;
import java.time.LocalDate;

/**
 * TaskFormatter formats tasks into human-readable lines using record pattern matching.
 */
public final class TaskFormatter {
	public String format(Task task) {
		return switch (task) {
			case BasicTask(long id, String title, String description, TaskState state, LocalDate dueDate) ->
				"Task: %s, State: %s, Due: %s".formatted(title, state, dueDate);
			case RecurringTask(long id, String title, String description, TaskState state, LocalDate dueDate, int interval) ->
				"Recurring Task: %s [%d days], State: %s, Due: %s".formatted(title, interval, state, dueDate);
		};
	}

	// Adapter for existing entity-based Task used elsewhere in the app
	public String format(il.ac.hit.tasksmanager.model.entities.Task legacyTask) {
		BasicTask adapted = new BasicTask(0L, legacyTask.title(), legacyTask.description(), legacyTask.state(), null);
		return format(adapted);
	}
}


