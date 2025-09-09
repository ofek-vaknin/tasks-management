package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.Task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy pattern: wraps a concrete {@link ITasksDAO} to provide a simple
 * read-through cache for get operations and invalidates on write operations.
 */
public class TasksDAOProxy implements ITasksDAO {
	private final ITasksDAO target; // underlying DAO
	private List<Task> cachedTasks; // cached list
	private final Map<Integer, Task> cachedById = new HashMap<>(); // cache by id

	/**
	 * Creates a proxy around the given DAO.
	 *
	 * @param target the underlying DAO to delegate to
	 */
	public TasksDAOProxy(ITasksDAO target) {
		this.target = target;
	}

	@Override
	/**
	 * Returns all tasks using a simple read-through cache.
	 *
	 * @return array of tasks
	 * @throws TasksDAOException on underlying DAO failure
	 */
	public synchronized Task[] getTasks() throws TasksDAOException {
		// Populate cache on first read and reuse until invalidated by a write
		if (cachedTasks == null) {
			cachedTasks = Arrays.asList(target.getTasks());
			cachedById.clear();
			// Index by id for O(1) future accesses in getTask
			for (Task t : cachedTasks) {
				cachedById.put(t.id(), t);
			}
		}
		return cachedTasks.toArray(new Task[0]);
	}

	@Override
	/**
	 * Returns a single task by id, served from cache when available.
	 *
	 * @param id task id
	 * @return task or null if not found
	 * @throws TasksDAOException on underlying DAO failure
	 */
	public synchronized Task getTask(int id) throws TasksDAOException {
		// Try cache first for O(1) lookup; fallback to target and populate cache
		if (cachedById.containsKey(id)) {
			return cachedById.get(id);
		}
		// Delegate to target and cache result if found
		Task t = target.getTask(id);
		if (t != null) {
			cachedById.put(id, t);
		}
		return t;
	}

	/**
	 * Invalidates both the list and by-id caches after write operations.
	 */
	private synchronized void invalidate() {
		cachedTasks = null;
		cachedById.clear();
	}

	@Override
	/**
	 * Delegates add to the target and invalidates caches.
	 *
	 * @param task task to add
	 * @return created task with id
	 * @throws TasksDAOException on failure
	 */
	public synchronized Task addTask(Task task) throws TasksDAOException {
		// Write-through followed by cache invalidation to avoid stale data
		Task created = target.addTask(task);
		invalidate();
		return created;
	}

	@Override
	/**
	 * Delegates update to the target and invalidates caches.
	 *
	 * @param task task to update
	 * @throws TasksDAOException on failure
	 */
	public synchronized void updateTask(Task task) throws TasksDAOException {
		// Forward update then drop caches to avoid stale reads
		target.updateTask(task);
		invalidate();
	}

	@Override
	/**
	 * Delegates delete by id and invalidates caches.
	 *
	 * @param id task id
	 * @throws TasksDAOException on failure
	 */
	public synchronized void deleteTask(int id) throws TasksDAOException {
		// Forward delete then drop caches
		target.deleteTask(id);
		invalidate();
	}

	@Override
	/**
	 * Delegates delete all and invalidates caches.
	 *
	 * @throws TasksDAOException on failure
	 */
	public synchronized void deleteTasks() throws TasksDAOException {
		// Drop all data downstream; ensure caches are cleared
		target.deleteTasks();
		invalidate();
	}
}


