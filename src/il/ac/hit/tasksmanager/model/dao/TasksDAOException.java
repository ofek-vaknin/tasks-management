package il.ac.hit.tasksmanager.model.dao;

/**
 * TasksDAOException represents failures in DAO operations (Derby access, SQL errors, etc.).
 */
public class TasksDAOException extends Exception {
	/** Creates a new DAO exception with a message. */
	public TasksDAOException(String message) {
		super(message);
	}

	/** Creates a new DAO exception with a message and a root cause. */
	public TasksDAOException(String message, Throwable cause) {
		super(message, cause);
	}
}


