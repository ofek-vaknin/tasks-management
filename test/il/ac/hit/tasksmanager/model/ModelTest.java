package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.entities.state.ToDoState;
import il.ac.hit.tasksmanager.model.observer.TaskObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {
    private static Model model;

    @BeforeAll
    public static void setup() throws ModelException {
        model = new Model();
    }

    @AfterAll
    public static void tearDown() {
        // no-op
    }

    @Test
    public void loadDataNotifiesObservers() throws InterruptedException {
        final boolean[] notified = { false };
        TaskObserver obs = () -> notified[0] = true;
        model.register(obs);
        model.loadData();
        Thread.sleep(200);
        assertTrue(notified[0]);
        model.remove(obs);
    }

    @Test
    public void addUpdateDeleteFlowNotifiesAndChangesCache() throws Exception {
        final int[] notifications = { 0 };
        TaskObserver obs = () -> notifications[0]++;
        model.register(obs);

        int initialSize = model.getTasks().size();

        model.addTask("Model Add Test", "desc");
        Thread.sleep(300);
        assertTrue(model.getTasks().size() >= initialSize + 1);
        assertTrue(notifications[0] >= 1);

        Task added = model.getTasks().stream().filter(t -> "Model Add Test".equals(t.title())).findFirst().orElse(null);
        assertNotNull(added);

        Task updated = new BasicTask(added.id(), "Model Update Test", added.description(), new ToDoState(), added.dueDate());
        model.updateTask(updated);
        Thread.sleep(300);
        Task fetched = model.getTasks().stream().filter(t -> t.id() == added.id()).findFirst().orElse(null);
        assertNotNull(fetched);
        assertEquals("Model Update Test", fetched.title());
        assertTrue(notifications[0] >= 2);

        model.deleteTask(added.id());
        Thread.sleep(300);
        Task deleted = model.getTasks().stream().filter(t -> t.id() == added.id()).findFirst().orElse(null);
        assertNull(deleted);
        assertTrue(notifications[0] >= 3);

        model.remove(obs);
    }
}





