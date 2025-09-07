package il.ac.hit.tasksmanager.model.patterns;

import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.RecurringTask;
import il.ac.hit.tasksmanager.model.entities.ToDoState;
import il.ac.hit.tasksmanager.model.entities.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TaskFormatterTest {
    @Test
    public void formatBasicTask() {
        TaskFormatter f = new TaskFormatter();
        var t = new BasicTask(1L, "Write report", "", new ToDoState(), LocalDate.of(2025, 6, 1));
        String line = f.format(t);
        assertTrue(line.contains("Write report"));
        assertTrue(line.contains("TODO"));
        assertTrue(line.contains("2025-06-01"));
    }

    @Test
    public void formatRecurringTask() {
        TaskFormatter f = new TaskFormatter();
        var t = new RecurringTask(2L, "Backup", "nightly", new ToDoState(), LocalDate.of(2025, 6, 2), 7);
        String line = f.format(t);
        assertTrue(line.contains("Recurring Task"));
        assertTrue(line.contains("Backup"));
        assertTrue(line.contains("7 days"));
    }

    @Test
    public void formatLegacyEntityAdapter() {
        TaskFormatter f = new TaskFormatter();
        il.ac.hit.tasksmanager.model.entities.Task legacy = new il.ac.hit.tasksmanager.model.entities.Task(0L, "Legacy", "", new ToDoState());
        String line = f.format(legacy);
        assertTrue(line.contains("Legacy"));
        assertTrue(line.contains("TODO"));
    }
}


