package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.entities.ITask;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import java.time.LocalDate;

/**
 * RecurringTask represents a task that repeats at a fixed interval of days.
 * Validation is performed in the canonical constructor.
 */
public record RecurringTask(int id, String title, String description, TaskState state, LocalDate dueDate, int interval)
        implements Task, ITask {
	/**
	 * Canonical constructor with validations for title, state and interval.
	 */
	public RecurringTask {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("title must not be empty");
		}
		if (state == null) {
			throw new IllegalArgumentException("state must not be null");
		}
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must be positive");
		}
	}
    @Override
    public int getId() {
        return id; }

    @Override
    public String getTitle() {
        return title; }

    @Override
    public String getDescription() {
        return description; }

    @Override
    public TaskState getState() {
        return state; }
}


