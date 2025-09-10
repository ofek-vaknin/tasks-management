package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.ITask;
import il.ac.hit.tasksmanager.model.entities.state.ToDoState;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TasksDAOImplTest {
    private static TasksDAOImpl dao;

    @BeforeAll
    public static void setup() throws TasksDAOException {
        dao = TasksDAOImpl.getInstance();
        // Clean slate for deterministic tests
        dao.deleteTasks();
    }

    @Test
    @Order(1)
    public void addAndGetTask() throws TasksDAOException {
        dao.addTask(new BasicTask(0, "Test A", "desc", new ToDoState(), LocalDate.of(2025, 6, 3)));
        ITask[] items = dao.getTasks();
        assertTrue(items.length > 0);
        int id = items[0].getId();

        ITask fetched = dao.getTask(id);
        assertNotNull(fetched);
        assertEquals("Test A", fetched.getTitle());
    }

    @Test
    @Order(2)
    public void updateTask() throws TasksDAOException {
        ITask[] all = dao.getTasks();
        assertTrue(all.length > 0);
        ITask first = all[0];
        ITask updated = new BasicTask(first.getId(), "Updated Title", first.getDescription(), first.getState(), (first instanceof Task t) ? t.dueDate() : null);
        dao.updateTask(updated);

        ITask fetched2 = dao.getTask(first.getId());
        assertEquals("Updated Title", fetched2.getTitle());
    }

    @Test
    @Order(3)
    public void deleteTask() throws TasksDAOException {
        dao.addTask(new BasicTask(0, "To Delete", null, new ToDoState(), null));
        ITask[] after = dao.getTasks();
        int delId = after[after.length - 1].getId();
        dao.deleteTask(delId);
        assertNull(dao.getTask(delId));
    }
}









