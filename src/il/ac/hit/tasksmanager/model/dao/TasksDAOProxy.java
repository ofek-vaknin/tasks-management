package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.Task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TasksDAOProxy caches read operations and invalidates on writes.
 */
public class TasksDAOProxy implements ITasksDAO {
	private final ITasksDAO target; // underlying DAO
	private List<Task> cachedTasks; // cached list
	private final Map<Long, Task> cachedById = new HashMap<>(); // cache by id

	public TasksDAOProxy(ITasksDAO target) {
		this.target = target;
	}

	@Override
	public synchronized Task[] getTasks() throws TasksDAOException {
		if (cachedTasks == null) {
			cachedTasks = Arrays.asList(target.getTasks());
			cachedById.clear();
			for (Task t : cachedTasks) {
				cachedById.put(t.id(), t);
			}
		}
		return cachedTasks.toArray(new Task[0]);
	}

	@Override
	public synchronized Task getTask(long id) throws TasksDAOException {
		if (cachedById.containsKey(id)) {
			return cachedById.get(id);
		}
		Task t = target.getTask(id);
		if (t != null) {
			cachedById.put(id, t);
		}
		return t;
	}

	private synchronized void invalidate() {
		cachedTasks = null;
		cachedById.clear();
	}

	@Override
	public synchronized Task addTask(Task task) throws TasksDAOException {
		Task created = target.addTask(task);
		invalidate();
		return created;
	}

	@Override
	public synchronized void updateTask(Task task) throws TasksDAOException {
		target.updateTask(task);
		invalidate();
	}

	@Override
	public synchronized void deleteTask(long id) throws TasksDAOException {
		target.deleteTask(id);
		invalidate();
	}

	@Override
	public synchronized void deleteTasks() throws TasksDAOException {
		target.deleteTasks();
		invalidate();
	}
}


