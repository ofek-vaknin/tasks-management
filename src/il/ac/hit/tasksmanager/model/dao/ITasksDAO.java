package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.Task;

/**
 * ITasksDAO defines CRUD operations for tasks persisted in Derby.
 */
public interface ITasksDAO {
	/** Returns all tasks sorted by ID. */
	Task[] getTasks() throws TasksDAOException;
	/** Returns a single task by ID or null if not found. */
	Task getTask(long id) throws TasksDAOException;
	/** Adds a new task and returns the created instance with generated ID. */
	Task addTask(Task task) throws TasksDAOException;
	/** Updates an existing task. */
	void updateTask(Task task) throws TasksDAOException;
	/** Deletes a task by ID. */
	void deleteTask(long id) throws TasksDAOException;
	/** Deletes all tasks. */
	void deleteTasks() throws TasksDAOException;
}


