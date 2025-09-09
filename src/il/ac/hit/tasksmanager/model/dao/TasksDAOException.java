package il.ac.hit.tasksmanager.model.dao;

/**
 * TasksDAOException represents failures in DAO operations
 * such as Derby database access or SQL errors.
 */
public class TasksDAOException extends Exception {

    /**
     * Constructs a new DAO exception with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public TasksDAOException(String message) {
        super(message);
    }

    /**
     * Constructs a new DAO exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception (can be null)
     */
    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
