package il.ac.hit.tasksmanager.model.entities.state;

/**
 * Concrete state representing IN_PROGRESS.
 */
public class InProgressState implements TaskState {
	@Override
	public String toString() {
		/*
		 * State pattern: concrete state value
		 * - Returns a stable symbolic name used across UI, filters, and persistence.
		 */
        return "IN_PROGRESS"; }
}


