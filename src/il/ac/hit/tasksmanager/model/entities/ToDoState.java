package il.ac.hit.tasksmanager.model.entities;

public final class ToDoState implements TaskState {
	@Override
	public String name() {
		return "TODO";
	}

	@Override
	public String toString() {
		return name();
	}
}


