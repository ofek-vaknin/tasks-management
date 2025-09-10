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
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;

/**
 * TasksListViewModel acts as the bridge between the Model and the Views
 * in the MVVM architecture.
 * It plays a dual role in the Observer pattern:
 * 1. As an Observer of the Model:
 *    - Implements TaskObserver.
 *    - Registers itself to Model.
 *    - Reacts to changes in the task data (add/update/delete/load)
 *      via onTasksChanged().
 * 2. As a Subject for the Views:
 *    - Maintains a list of registered ViewModelObservers (typically Views).
 *    - Notifies them (via update()) whenever the task list changes.
 *    - Ensures that UI updates happen on the Swing EDT
 *      using SwingUtilities.invokeLater.
 * In summary:
 * - Observer facing the Model.
 * - Subject facing the Views.
 * - This makes TasksListViewModel a classic ViewModel in MVVM,
 *   translating domain changes into UI updates.
 */

public class TasksListViewModel implements TaskObserver {
	private IModel model;
	private final List<ViewModelObserver> observers = new ArrayList<>();

	public TasksListViewModel() throws ModelException {
		/*
		 * Initialize ViewModel with a concrete Model using a setter
		 * to centralize validation and future wiring.
		 */
		setModel(new Model());
		this.model.register(this);
		this.model.loadData();
	}

	/** Sets the backing model (must not be null). */
	public synchronized void setModel(IModel model) {
		/*
		 * Subject/Observer wiring point:
		 * - Validate and assign the model reference used by this ViewModel.
		 */
		if (model == null) {
			throw new IllegalArgumentException("model must not be null");
		}
		this.model = model;
	}

	/** Returns the current list of tasks from the model. */
	public List<Task> getTasks() {
		return model.getTasks();
	}

	/** Returns tasks matching the provided filter. */
	public List<Task> getFilteredTasks(TaskFilter filter) {
		return model.getTasks().stream().filter(filter::matches).collect(Collectors.toList());
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
		/*
		 * Subject side of Observer: allow views to subscribe for updates
		 * from this ViewModel.
		 */
		observers.add(observer);
	}

	private void notifyObservers() {
		/*
		 * Notify subscribed views (ViewModelObserver) that data has changed.
		 */
		for (ViewModelObserver o : observers) {
			o.update();
		}
	}

	@Override
	public void onTasksChanged() {
		/*
		 * Observer callback from Model -> re-dispatch to views on EDT.
		 */
		SwingUtilities.invokeLater(this::notifyObservers);
	}
}


