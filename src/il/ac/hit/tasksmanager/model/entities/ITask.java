package il.ac.hit.tasksmanager.model.entities;

public interface ITask {
	long id();
	String title();
	String description();
	TaskState state();
}


