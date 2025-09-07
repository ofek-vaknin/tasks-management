package il.ac.hit.tasksmanager.model.patterns;

import il.ac.hit.tasksmanager.model.entities.Task;

public final class ConsoleReportVisitor {
	private final TaskFormatter formatter = new TaskFormatter();

	public void print(Task task) {
		System.out.println(formatter.format(task));
	}
}


