package il.ac.hit.tasksmanager.viewmodel;

import il.ac.hit.tasksmanager.model.IModel;
import il.ac.hit.tasksmanager.model.Model;
import il.ac.hit.tasksmanager.model.ModelException;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.combinator.TaskFilter;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import il.ac.hit.tasksmanager.model.entities.state.ToDoState;
import il.ac.hit.tasksmanager.model.observer.TaskObserver;
import il.ac.hit.tasksmanager.viewmodel.observer.ViewModelObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * TasksListViewModel exposes tasks and asynchronous operations to Swing views,
 * forwarding calls to {@link il.ac.hit.tasksmanager.model.IModel} and notifying
 * registered observers on updates.
 */
public class TasksListViewModel implements TaskObserver {
	private final IModel model; // model dependency
	private final List<ViewModelObserver> observers = new ArrayList<>(); // view observers

	public TasksListViewModel() throws ModelException {
		this.model = new Model();
		this.model.register(this);
		this.model.loadData();
	}

	/** Returns the current list of tasks from the model. */
	public List<Task> getTasks() {
		return model.getTasks();
	}

	/** Returns tasks matching the provided filter. */
	public List<Task> getFilteredTasks(TaskFilter filter) {
		return model.getTasks().stream().filter(filter::matches).collect(Collectors.toList());
	}

	/** Fetches tasks asynchronously and passes them to the callback. */
	public void getTasksAsync(Consumer<Task[]> callback) {
		model.getTasksAsync(callback);
	}

	/** Adds a new basic task with default state. */
	public void addTask(String title, String description) throws ModelException {
		model.addTask(title, description);
	}

	/** Adds a basic task with explicit state and due date. */
	public void addTask(String title, String description, TaskState state, java.time.LocalDate dueDate) throws ModelException {
		if (state == null) {
			state = new ToDoState();
		}
		model.addTask(title, description, state, dueDate);
	}

	/** Adds a recurring task with validation on interval and due date. */
	public void addRecurringTask(String title, String description, TaskState state, java.time.LocalDate dueDate, int recurrenceDays) throws ModelException {
		if (state == null) {
			state = new ToDoState();
		}
		if (recurrenceDays <= 0) {
			throw new ModelException("recurrenceDays must be positive");
		}
		if (dueDate != null && dueDate.isBefore(java.time.LocalDate.now())) {
			throw new ModelException("Due date must be today or in the future");
		}
		if (model instanceof Model m) {
			m.addRecurringTask(title, description, state, dueDate, recurrenceDays);
		} else {
			throw new ModelException("Model does not support recurring tasks");
		}
	}

	/** Deletes a task by id. */
	public void deleteTask(int id) throws ModelException {
		model.deleteTask(id);
	}

	/** Updates the given task. */
	public void updateTask(Task task) throws ModelException {
		model.updateTask(task);
	}

	/** Registers a view observer. */
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


