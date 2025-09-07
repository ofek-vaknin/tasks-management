package il.ac.hit.tasksmanager.model.patterns;

@FunctionalInterface
public interface TaskObserver {
	void onTasksChanged();
}


