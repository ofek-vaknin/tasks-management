package il.ac.hit.tasksmanager.model.entities.state;

/**
 * Concrete state representing COMPLETED.
 */
public class CompletedState implements TaskState {
	@Override
	public String toString() {
		/*
		 * State pattern: concrete state value
		 * - Returns a stable symbolic name used across UI, filters, and persistence.
		 */
        return "COMPLETED"; }
}


