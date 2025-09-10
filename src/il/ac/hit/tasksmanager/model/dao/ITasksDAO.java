package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.entities.ITask;

/**
 * ITasksDAO defines CRUD operations for tasks persisted in Derby.
 * Matches the lecturer's required interface signatures.
 */
public interface ITasksDAO {

    /**
     * Returns all tasks sorted by ID.
     *
     * @return array of tasks
     * @throws TasksDAOException if a database access error occurs
     */
    ITask[] getTasks() throws TasksDAOException;

    /**
     * Returns a single task by ID or null if not found.
     *
     * @param id the ID of the task to retrieve
     * @return the task instance or null if not found
     * @throws TasksDAOException if a database access error occurs
     */
    ITask getTask(int id) throws TasksDAOException;

    /**
     * Adds a new task.
     *
     * @param task the task to add
     * @throws TasksDAOException if a database access error occurs
     */
    void addTask(ITask task) throws TasksDAOException;

    /**
     * Updates an existing task.
     *
     * @param task the task to update
     * @throws TasksDAOException if a database access error occurs
     */
    void updateTask(ITask task) throws TasksDAOException;

    /**
     * Deletes a task by ID.
     *
     * @param id the ID of the task to delete
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
