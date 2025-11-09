package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.ITask;
import il.ac.hit.tasksmanager.model.entities.state.ToDoState;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the caching/invalidation behavior of TasksDAOProxy.
 */
public class TasksDAOProxyTest {

    /**
     * A minimal fake DAO to count calls and provide predictable data.
     */
    static class FakeDAO implements ITasksDAO {
        int getTasksCalls = 0;
        int getTaskCalls = 0;
        int addCalls = 0;
        int updateCalls = 0;
        int deleteCalls = 0;
        int deleteAllCalls = 0;
        BasicTask stored = new BasicTask(1, "A", null, new ToDoState(), null);

        @Override
        public ITask[] getTasks() {
            getTasksCalls++;
            return stored == null ? new ITask[0] : new ITask[]{ stored };
        }

        @Override
        public ITask getTask(int id) {
            getTaskCalls++;
            if (stored != null && stored.id() == id) return stored;
            return null;
        }

        @Override
        public void addTask(ITask task) {
            addCalls++;
            if (task instanceof Task mt) {
                stored = new BasicTask(2, mt.title(), mt.description(), mt.state(), mt.dueDate());
            } else {
                stored = new BasicTask(2, task.getTitle(), task.getDescription(), task.getState(), null);
            }
        }

        @Override
        public void updateTask(ITask task) {
            updateCalls++;
            if (task instanceof Task mt) {
                stored = new BasicTask(mt.id(), mt.title(), mt.description(), mt.state(), mt.dueDate());
            } else {
                stored = new BasicTask(task.getId(), task.getTitle(), task.getDescription(), task.getState(), null);
            }
        }

        @Override
        public void deleteTask(int id) {
            deleteCalls++;
            if (stored != null && stored.id() == id) stored = null;
        }

        @Override
        public void deleteTasks() {
            deleteAllCalls++;
            stored = null;
        }
    }

    @Test
    public void cachesGetTasksAndGetTaskUntilInvalidated() throws TasksDAOException {
        FakeDAO fake = new FakeDAO();
        TasksDAOProxy proxy = new TasksDAOProxy(fake);

        // First call should hit underlying DAO
        ITask[] first = proxy.getTasks();
        assertEquals(1, fake.getTasksCalls);
        assertEquals(1, first.length);

        // Second call should be served from cache
        ITask[] second = proxy.getTasks();
        assertEquals(1, fake.getTasksCalls, "getTasks should be cached");
        assertEquals(1, second.length);

        // getTask by id should hit cache and not call underlying getTask
        ITask t1 = proxy.getTask(1);
        assertNotNull(t1);
        assertEquals(0, fake.getTaskCalls, "getTask should be served from id cache");

        // Add a task -> invalidates cache
        proxy.addTask(new BasicTask(0, "B", "desc", new ToDoState(), LocalDate.now()));
        assertEquals(1, fake.addCalls);

        // After invalidation, getTasks should call underlying again
        ITask[] afterAdd = proxy.getTasks();
        assertEquals(2, fake.getTasksCalls, "getTasks should reload after add");
        assertEquals(1, afterAdd.length);

        // Update -> invalidation again
        proxy.updateTask(new BasicTask(afterAdd[0].getId(), "B2", null, new ToDoState(), null));
        assertEquals(1, fake.updateCalls);
        proxy.getTasks();
        assertEquals(3, fake.getTasksCalls, "getTasks should reload after update");

        // Delete by id -> invalidation again
        proxy.deleteTask(afterAdd[0].getId());
        assertEquals(1, fake.deleteCalls);
        proxy.getTasks();
        assertEquals(4, fake.getTasksCalls, "getTasks should reload after delete");

        // Delete all -> invalidation again
        proxy.deleteTasks();
        assertEquals(1, fake.deleteAllCalls);
        proxy.getTasks();
        assertEquals(5, fake.getTasksCalls, "getTasks should reload after delete all");
    }
}


