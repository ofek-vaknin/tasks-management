package il.ac.hit.tasksmanager.model.entities;

public final class CompletedState implements TaskState {
	@Override
	public String name() {
		return "COMPLETED";
	}

	@Override
	public String toString() {
		return name();
	}
}


