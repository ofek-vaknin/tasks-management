package il.ac.hit.tasksmanager.model.patterns;

import il.ac.hit.tasksmanager.model.entities.Task;

import java.util.function.Predicate;

public interface TaskPredicate extends Predicate<Task> {
	default TaskPredicate and(TaskPredicate other) {
		return task -> this.test(task) && other.test(task);
	}

	default TaskPredicate or(TaskPredicate other) {
		return task -> this.test(task) || other.test(task);
	}

	static TaskPredicate titleContains(String keyword) {
		final String needle = keyword == null ? "" : keyword.toLowerCase();
		return t -> t.title() != null && t.title().toLowerCase().contains(needle);
	}

	static TaskPredicate hasState(String stateName) {
		return t -> t.state() != null && stateName.equalsIgnoreCase(t.state().name());
	}
}


