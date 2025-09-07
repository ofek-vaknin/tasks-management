package il.ac.hit.tasksmanager.model.patterns;

import il.ac.hit.tasksmanager.model.entities.Task;

public class TitleFilter implements TaskPredicate {
	private final String keyword;

	public TitleFilter(String keyword) {
		this.keyword = keyword == null ? "" : keyword.toLowerCase();
	}

    @Override
    public boolean test(Task task) {
        return task.title() != null && task.title().toLowerCase().contains(keyword);
    }
}


