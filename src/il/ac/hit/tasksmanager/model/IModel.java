package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.patterns.TaskObserver;

import java.util.List;
import java.util.function.Consumer;

/**
 * IModel defines the application data operations with asynchronous access and observer notifications.
 */
public interface IModel {
	/** Loads tasks asynchronously into cache and notifies observers. */
	void loadData();
	/** Saves data (no-op if persistence is handled per mutation). */
	void saveData();

	/** Returns an immutable view of the cached tasks. */
	List<Task> getTasks();

	/** Retrieves tasks asynchronously and invokes the callback on the EDT. */
	void getTasksAsync(Consumer<Task[]> callback);

	/** Adds a new task with default state and null due date. */
	void addTask(String title, String description) throws ModelException;
	/** Adds a new task with the specified state and due date. */
	void addTask(String title, String description, il.ac.hit.tasksmanager.model.entities.TaskState state, java.time.LocalDate dueDate) throws ModelException;
	/** Updates an existing task by ID. */
	void updateTask(Task task) throws ModelException;
	/** Deletes a task by ID. */
	void deleteTask(long id) throws ModelException;

	/** Registers an observer for model changes. */
	void register(TaskObserver observer);
	/** Unregisters the given observer. */
	void remove(TaskObserver observer);
}


