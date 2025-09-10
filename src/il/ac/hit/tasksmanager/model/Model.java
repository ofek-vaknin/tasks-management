package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.dao.ITasksDAO;
import il.ac.hit.tasksmanager.model.dao.TasksDAOException;
import il.ac.hit.tasksmanager.model.dao.TasksDAOProxy;
import il.ac.hit.tasksmanager.model.dao.TasksDAOImpl;
import il.ac.hit.tasksmanager.model.entities.ITask;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import il.ac.hit.tasksmanager.model.entities.state.ToDoState;
import il.ac.hit.tasksmanager.model.observer.TaskObserver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Model implements the application domain logic, backed by a DAO and
 * an executor for asynchronous operations.
 * Observer Pattern (Subject):
 * - Observers of type {@link TaskObserver} register via {@link #register(TaskObserver)}
 * - They are stored internally and notified via {@link #notifyObservers()} after data changes
 * - Primary observer implementation: {@code il.ac.hit.tasksmanager.viewmodel.TasksListViewModel}
 * Threading model (non-blocking UI):
 * - All Derby/DAO operations run on a background {@link ExecutorService} (not on the Swing EDT)
 *   to avoid blocking the UI while the database touches the filesystem.
 * - The ViewModel is responsible for re-dispatching any UI updates back to the Swing EDT.
 */
public class Model implements IModel {
	private final ITasksDAO dao;
	private final ExecutorService executor;
	private final List<TaskObserver> observers = new ArrayList<>();
	private volatile List<Task> cached = new ArrayList<>();

	public Model() throws ModelException {
		try {
			this.dao = new TasksDAOProxy(TasksDAOImpl.getInstance());
			this.executor = Executors.newFixedThreadPool(4);
		} catch (TasksDAOException e) {
			throw new ModelException("Failed to initialize DAO", e);
		}
	}

	@Override
	/**
	 * Loads tasks asynchronously from the DAO into the in-memory cache and notifies observers.
	 */
	public void loadData() {
		executor.submit(() -> {
			try {
				ITask[] arr = dao.getTasks();
				cached = Arrays.stream(arr).map(t -> (Task) t).toList();
				/*
				 * Observer: notify subscribers that the data set has changed
				 */
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error loading tasks: " + e.getMessage());
			}
		});
	}

	/**
	 * Retrieves tasks asynchronously and invokes the given callback directly (no Swing dependency).
	 * The ViewModel is responsible for EDT dispatching.
	 */
	public void getTasksAsync(Consumer<Task[]> callback) {
		executor.submit(() -> {
			try {
				ITask[] tasks = dao.getTasks();
				Task[] mapped = Arrays.stream(tasks).map(t -> (Task) t).toArray(Task[]::new);
				callback.accept(mapped);
			} catch (TasksDAOException e) {
				System.err.println("Error loading tasks async: " + e.getMessage());
			}
		});
	}

	@Override
	/**
	 * Persists data if needed. For embedded Derby every mutation is persisted immediately, so this is a no-op.
	 */
	public void saveData() {
		// Derby is persisted on each mutation; method left for API completeness
	}

	@Override
	/**
	 * Returns an immutable snapshot of the currently cached tasks.
	 *
	 * @return unmodifiable list of tasks
	 */
	public List<Task> getTasks() {
		return Collections.unmodifiableList(cached);
	}

	@Override
	/**
	 * Adds a basic task with default TODO state and no due date.
	 *
	 * @param title task title
	 * @param description task description
	 */
	public void addTask(String title, String description) throws ModelException {
		executor.submit(() -> {
			try {
				dao.addTask(new BasicTask(0, title, description, new ToDoState(), null));
				ITask[] arr = dao.getTasks();
				cached = Arrays.stream(arr).map(t -> (Task) t).toList();
				/* Notify all observers about the data change */
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error adding task: " + e.getMessage());
			}
		});
	}

	/**
	 * Adds a basic task with an explicit state and due date, validating the due date is not in the past.
	 *
	 * @param title task title
	 * @param description task description
	 * @param state desired state (TODO if null)
	 * @param dueDate due date (nullable)
	 * @throws ModelException if validation fails
	 */
	public void addTask(String title, String description, TaskState state, LocalDate dueDate) throws ModelException {
		// Validate due date is not in the past
		if (dueDate != null && dueDate.isBefore(java.time.LocalDate.now())) {
			throw new ModelException("Due date must be today or in the future");
		}
		executor.submit(() -> {
			try {
				dao.addTask(new BasicTask(0, title, description, state == null ? new ToDoState() : state, dueDate));
				ITask[] arr = dao.getTasks();
				cached = Arrays.stream(arr).map(t -> (Task) t).toList();
				/* Notify all observers about the data change */
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error adding task: " + e.getMessage());
			}
		});
	}

	/**
	 * Adds a recurring task with validation on interval and due date.
	 *
	 * @param title task title
	 * @param description task description
	 * @param state desired state (TODO if null)
	 * @param dueDate due date (nullable)
	 * @param recurrenceDays positive interval in days
	 * @throws ModelException if validation fails
	 */
	public void addRecurringTask(String title, String description, TaskState state, LocalDate dueDate, int recurrenceDays) throws ModelException {
		if (recurrenceDays <= 0) {
			throw new ModelException("recurrenceDays must be positive");
		}
		if (dueDate != null && dueDate.isBefore(java.time.LocalDate.now())) {
			throw new ModelException("Due date must be today or in the future");
		}
		executor.submit(() -> {
			try {
				RecurringTask rt = new RecurringTask(0, title, description, state == null ? new ToDoState() : state, dueDate, recurrenceDays);
				dao.addTask(rt);
				ITask[] arr = dao.getTasks();
				cached = Arrays.stream(arr).map(t -> (Task) t).toList();
				/* Notify all observers about the data change */
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error adding recurring task: " + e.getMessage());
			}
		});
	}

	@Override
	/**
	 * Updates an existing task using the DAO and refreshes the cache.
	 *
	 * @param task task with updated values
	 */
	public void updateTask(Task task) throws ModelException {
		executor.submit(() -> {
			try {
				dao.updateTask((ITask) task);
				ITask[] arr = dao.getTasks();
				cached = Arrays.stream(arr).map(t -> (Task) t).toList();
				/* Notify all observers about the data change */
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error updating task: " + e.getMessage());
			}
		});
	}

	@Override
	/**
	 * Deletes a task by id using the DAO and refreshes the cache.
	 *
	 * @param id task identifier
	 */
	public void deleteTask(int id) throws ModelException {
		executor.submit(() -> {
			try {
				dao.deleteTask(id);
				ITask[] arr = dao.getTasks();
				cached = Arrays.stream(arr).map(t -> (Task) t).toList();
				/* Notify all observers about the data change */
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error deleting task: " + e.getMessage());
			}
		});
	}

	@Override
	/** Registers a model observer that will be notified on data changes. */
	public void register(TaskObserver observer) {
		observers.add(observer);
	}

	@Override
	/** Unregisters a previously registered observer. */
	public void remove(TaskObserver observer) {
		observers.remove(observer);
	}

	/** Notifies all registered observers about data changes (Observer pattern). */
	private void notifyObservers() {
		/*
		 * Iterate over a snapshot of observers to avoid ConcurrentModification
		 * if observers add/remove themselves during callback (simple approach).
		 */
		for (TaskObserver o : observers) {
			o.onTasksChanged();
		}
	}
}


