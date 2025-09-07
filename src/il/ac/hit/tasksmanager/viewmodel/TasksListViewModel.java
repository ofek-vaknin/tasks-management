package il.ac.hit.tasksmanager.viewmodel;

import il.ac.hit.tasksmanager.model.IModel;
import il.ac.hit.tasksmanager.model.Model;
import il.ac.hit.tasksmanager.model.ModelException;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.patterns.TaskObserver;
import il.ac.hit.tasksmanager.model.combinator.TaskFilter;
import il.ac.hit.tasksmanager.model.entities.TaskState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import il.ac.hit.tasksmanager.viewmodel.observer.ViewModelObserver;

/**
 * TasksListViewModel exposes tasks and async operations to the Swing views.
 */
public class TasksListViewModel implements TaskObserver {
	private final IModel model;
	private final List<ViewModelObserver> observers = new ArrayList<>();

	public TasksListViewModel() throws ModelException {
		this.model = new Model();
		this.model.register(this);
		this.model.loadData();
	}

	public List<Task> getTasks() {
		return model.getTasks();
	}

	public List<Task> getFilteredTasks(TaskFilter filter) {
		return model.getTasks().stream().filter(filter::matches).collect(Collectors.toList());
	}

	public void getTasksAsync(Consumer<Task[]> callback) {
		model.getTasksAsync(callback);
	}

	public void addTask(String title, String description) throws ModelException {
		model.addTask(title, description);
	}

	public void addTask(String title, String description, TaskState state, java.time.LocalDate dueDate) throws ModelException {
		if (state == null) {
			state = new il.ac.hit.tasksmanager.model.entities.ToDoState();
		}
		model.addTask(title, description, state, dueDate);
	}

	public void deleteTask(long id) throws ModelException {
		model.deleteTask(id);
	}

	public void updateTask(Task task) throws ModelException {
		model.updateTask(task);
	}

	public void registerObserver(ViewModelObserver observer) {
		observers.add(observer);
	}

	private void notifyObservers() {
		for (ViewModelObserver o : observers) {
			o.update();
		}
	}

	@Override
	public void onTasksChanged() {
		notifyObservers();
	}
}


