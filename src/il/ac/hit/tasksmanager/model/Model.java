package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.dao.ITasksDAO;
import il.ac.hit.tasksmanager.model.dao.TasksDAOException;
import il.ac.hit.tasksmanager.model.dao.TasksDAOProxy;
import il.ac.hit.tasksmanager.model.dao.TasksDAOImpl;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.entities.ToDoState;
import il.ac.hit.tasksmanager.model.patterns.TaskObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Model implements IModel with a DAO backend and an executor for async operations.
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
	public void loadData() {
		executor.submit(() -> {
			try {
				cached = Arrays.asList(dao.getTasks());
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error loading tasks: " + e.getMessage());
			}
		});
	}

	public void getTasksAsync(Consumer<Task[]> callback) {
		executor.submit(() -> {
			try {
				Task[] tasks = dao.getTasks();
				SwingUtilities.invokeLater(() -> callback.accept(tasks));
			} catch (TasksDAOException e) {
				System.err.println("Error loading tasks async: " + e.getMessage());
			}
		});
	}

	@Override
	public void saveData() {
		// Derby is persisted on each mutation; method left for API completeness
	}

	@Override
	public List<Task> getTasks() {
		return Collections.unmodifiableList(cached);
	}

	@Override
	public void addTask(String title, String description) throws ModelException {
		executor.submit(() -> {
			try {
				dao.addTask(new BasicTask(0L, title, description, new ToDoState(), null));
				cached = Arrays.asList(dao.getTasks());
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error adding task: " + e.getMessage());
			}
		});
	}

	public void addTask(String title, String description, il.ac.hit.tasksmanager.model.entities.TaskState state, java.time.LocalDate dueDate) throws ModelException {
		executor.submit(() -> {
			try {
				dao.addTask(new BasicTask(0L, title, description, state == null ? new ToDoState() : state, dueDate));
				cached = Arrays.asList(dao.getTasks());
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error adding task: " + e.getMessage());
			}
		});
	}

	@Override
	public void updateTask(Task task) throws ModelException {
		executor.submit(() -> {
			try {
				dao.updateTask(task);
				cached = Arrays.asList(dao.getTasks());
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error updating task: " + e.getMessage());
			}
		});
	}

	@Override
	public void deleteTask(long id) throws ModelException {
		executor.submit(() -> {
			try {
				dao.deleteTask(id);
				cached = Arrays.asList(dao.getTasks());
				notifyObservers();
			} catch (TasksDAOException e) {
				System.err.println("Error deleting task: " + e.getMessage());
			}
		});
	}

	@Override
	public void register(TaskObserver observer) {
		observers.add(observer);
	}

	@Override
	public void remove(TaskObserver observer) {
		observers.remove(observer);
	}

	private void notifyObservers() {
		for (TaskObserver o : observers) {
			o.onTasksChanged();
		}
	}
}


