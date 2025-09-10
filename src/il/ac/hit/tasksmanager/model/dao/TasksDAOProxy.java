package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.ITask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy pattern: wraps a concrete {@link ITasksDAO} to provide a simple
 * read-through cache for get operations and invalidates on write operations.
 */
public class TasksDAOProxy implements ITasksDAO {
	private ITasksDAO target;
	private List<ITask> cachedTasks;
	private final Map<Integer, ITask> cachedById = new HashMap<>();

	/**
	 * Creates a proxy around the given DAO.
	 *
	 * @param target the underlying DAO to delegate to
	 */
	public TasksDAOProxy(ITasksDAO target) {
		/*
		 * Proxy constructor
		 * - Delegates target assignment to setTarget(...) to centralize validation
		 *   and to ensure caches are invalidated consistently on target change.
		 */
		setTarget(target);
	}

	/**
	 * Sets the underlying DAO target with validation and clears caches.
	 *
	 * @param target the DAO to delegate to (must not be null)
	 * @throws IllegalArgumentException if target is null
	 */
	public synchronized void setTarget(ITasksDAO target) {
		/*
		 * Validation + cache reset
		 * - Validate non-null delegate
		 * - Assign the new delegate
		 * - Invalidate in-memory caches to avoid serving mixed data from the
		 *   previous delegate.
		 */
		if (target == null) {
			throw new IllegalArgumentException("target DAO must not be null");
		}
		this.target = target;
		invalidate();
	}

	/**
	 * Returns all tasks using a simple read-through cache.
	 *
	 * @return array of tasks
	 * @throws TasksDAOException on underlying DAO failure
	 */
	@Override
	public synchronized ITask[] getTasks() throws TasksDAOException {
		/*
		 * Read-through list cache
		 * - On first access (or after invalidation), delegate to the target DAO,
		 *   snapshot the results in-memory, and build an ID index for O(1) lookups.
		 * - Subsequent calls return the cached snapshot for fast reads until
		 *   a write operation invalidates the cache.
		 */
		if (cachedTasks == null) {
			cachedTasks = Arrays.asList(target.getTasks());
			cachedById.clear();
			for (ITask t : cachedTasks) {
				cachedById.put(t.getId(), t);
			}
		}
		return cachedTasks.toArray(new ITask[0]);
	}

	/**
	 * Returns a single task by id, served from cache when available.
	 *
	 * @param id task id
	 * @return task or null if not found
	 * @throws TasksDAOException on underlying DAO failure
	 */
	@Override
	public synchronized ITask getTask(int id) throws TasksDAOException {
		/*
		 * By-ID cache with fallback
		 * - Attempt O(1) lookup from the in-memory index.
		 * - If missing, ask the delegate then cache the result (if found) to
		 *   optimize subsequent accesses.
		 */
		if (cachedById.containsKey(id)) {
			return cachedById.get(id);
		}
		ITask t = target.getTask(id);
		if (t != null) {
			cachedById.put(id, t);
		}
		return t;
	}

	/**
	 * Invalidates both the list and by-id caches after write operations.
	 */
	private synchronized void invalidate() {
		/*
		 * Cache invalidation policy
		 * - Drop the list snapshot and the ID index so the next read will
		 *   refresh from the authoritative data source (delegate DAO).
		 */
		cachedTasks = null;
		cachedById.clear();
	}

	/**
	 * Delegates add to the target and invalidates caches.
	 *
	 * @param task task to add
	 * @return created task with id
	 * @throws TasksDAOException on failure
	 */
	@Override
	public synchronized void addTask(ITask task) throws TasksDAOException {
		/*
		 * Write-through + invalidate
		 * - Forward the mutation to the delegate DAO.
		 * - Invalidate caches to prevent stale reads on subsequent get operations.
		 */
		target.addTask(task);
		invalidate();
	}

	/**
	 * Delegates update to the target and invalidates caches.
	 *
	 * @param task task to update
	 * @throws TasksDAOException on failure
	 */
	@Override
	public synchronized void updateTask(ITask task) throws TasksDAOException {
		/*
		 * Write-through + invalidate
		 * - Forward the update to the delegate DAO.
		 * - Invalidate caches to ensure follow-up reads observe the latest state.
		 */
		target.updateTask(task);
		invalidate();
	}

	/**
	 * Delegates delete by id and invalidates caches.
	 *
	 * @param id task id
	 * @throws TasksDAOException on failure
	 */
	@Override
	public synchronized void deleteTask(int id) throws TasksDAOException {
		/*
		 * Write-through + invalidate
		 * - Forward deletion to the delegate DAO.
		 * - Invalidate caches to avoid serving deleted items from memory.
		 */
		target.deleteTask(id);
		invalidate();
	}

	/**
	 * Delegates delete all and invalidates caches.
	 *
	 * @throws TasksDAOException on failure
	 */
	@Override
	public synchronized void deleteTasks() throws TasksDAOException {
		/*
		 * Bulk delete + invalidate
		 * - Forward mass deletion to the delegate DAO and clear caches.
		 */
		target.deleteTasks();
		invalidate();
	}
}


