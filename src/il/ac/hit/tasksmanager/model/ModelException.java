package il.ac.hit.tasksmanager.model;

/**
 * Project-specific exception representing model validation or operation failures.
 */
public class ModelException extends Exception {
	/** Creates a new model exception with a message. */
	public ModelException(String message) {
		super(message);
	}

	/** Creates a new model exception with a message and root cause. */
	public ModelException(String message, Throwable cause) {
		super(message, cause);
	}
}


