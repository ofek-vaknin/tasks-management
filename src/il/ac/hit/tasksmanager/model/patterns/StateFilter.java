package il.ac.hit.tasksmanager.model.patterns;

import il.ac.hit.tasksmanager.model.entities.Task;

public class StateFilter implements TaskPredicate {
	private final String stateName;

	public StateFilter(String stateName) {
		this.stateName = stateName;
	}

    @Override
    public boolean test(Task task) {
        return task.state() != null && stateName.equalsIgnoreCase(task.state().name());
    }
}


