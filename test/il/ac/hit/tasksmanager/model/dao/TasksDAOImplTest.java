package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.ToDoState;
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
        Task created = dao.addTask(new BasicTask(0L, "Test A", "desc", new ToDoState(), LocalDate.of(2025, 6, 3)));
        assertTrue(created.id() > 0);

        Task fetched = dao.getTask(created.id());
        assertNotNull(fetched);
        assertEquals("Test A", fetched.title());
    }

    @Test
    @Order(2)
    public void updateTask() throws TasksDAOException {
        Task[] all = dao.getTasks();
        assertTrue(all.length > 0);
        Task first = all[0];
        Task updated = new BasicTask(first.id(), "Updated Title", first.description(), first.state(), first.dueDate());
        dao.updateTask(updated);

        Task fetched = dao.getTask(first.id());
        assertEquals("Updated Title", fetched.title());
    }

    @Test
    @Order(3)
    public void deleteTask() throws TasksDAOException {
        Task t = dao.addTask(new BasicTask(0L, "To Delete", null, new ToDoState(), null));
        dao.deleteTask(t.id());
        assertNull(dao.getTask(t.id()));
    }
}


