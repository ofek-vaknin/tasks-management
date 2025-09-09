package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.Task;

/**
 * ITasksDAO defines CRUD operations for tasks persisted in Derby.
 */
public interface ITasksDAO {

    /**
     * Returns all tasks sorted by ID.
     *
     * @return array of tasks
     * @throws TasksDAOException if a database access error occurs
     */
    Task[] getTasks() throws TasksDAOException;

    /**
     * Returns a single task by ID or null if not found.
     *
     * @param id the ID of the task to retrieve
     * @return the Task instance or null if not found
     * @throws TasksDAOException if a database access error occurs
     */
    Task getTask(int id) throws TasksDAOException;

    /**
     * Adds a new task and returns the created instance with generated ID.
     *
     * @param task the Task to add
     * @return the created Task with generated ID
     * @throws TasksDAOException if a database access error occurs
     */
    Task addTask(Task task) throws TasksDAOException;

    /**
     * Updates an existing task.
     *
     * @param task the Task to update
     * @throws TasksDAOException if a database access error occurs
     */
    void updateTask(Task task) throws TasksDAOException;

    /**
     * Deletes a task by ID.
     *
     * @param id the ID of the Task to delete
     * @throws TasksDAOException if a database access error occurs
     */
    void deleteTask(int id) throws TasksDAOException;

    /**
     * Deletes all tasks.
     *
     * @throws TasksDAOException if a database access error occurs
     */
    void deleteTasks() throws TasksDAOException;
}
