package il.ac.hit.tasksmanager.model.entities;

public sealed interface TaskState permits ToDoState, InProgressState, CompletedState {
	String name();
}


