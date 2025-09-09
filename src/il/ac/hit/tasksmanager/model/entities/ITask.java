package il.ac.hit.tasksmanager.model.entities;

import il.ac.hit.tasksmanager.model.entities.state.TaskState;

/**
 * ITask is the lecturer-specified interface for tasks (int id, getters).
 * Implemented by model records and the legacy entity.
 */
public interface ITask {
	/** Returns the identifier as int (clamped if needed). */
	int getId();
	/** Returns the task title. */
	String getTitle();
	/** Returns the description. */
	String getDescription();
	/** Returns the current state. */
	TaskState getState();
}


