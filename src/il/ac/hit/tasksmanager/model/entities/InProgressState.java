package il.ac.hit.tasksmanager.model.entities;

public final class InProgressState implements TaskState {
	@Override
	public String name() {
		return "IN_PROGRESS";
	}

	@Override
	public String toString() {
		return name();
	}
}


