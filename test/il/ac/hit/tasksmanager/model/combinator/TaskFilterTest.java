package il.ac.hit.tasksmanager.model.combinator;

import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.state.ToDoState;
import il.ac.hit.tasksmanager.model.entities.state.InProgressState;
import il.ac.hit.tasksmanager.model.entities.state.CompletedState;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TaskFilterTest {
    @Test
    public void matchesByTitle() {
        Task t = new BasicTask(1L, "Buy milk", "2%", new ToDoState(), LocalDate.of(2025, 1, 10));
        assertTrue(TaskFilter.byTitle("milk").matches(t));
        assertFalse(TaskFilter.byTitle("bread").matches(t));
    }

    @Test
    public void matchesByStateCaseInsensitive() {
        Task t = new BasicTask(2L, "Implement feature", null, new InProgressState(), null);
        assertTrue(TaskFilter.byState("in_progress").matches(t));
        assertFalse(TaskFilter.byState("completed").matches(t));
    }

    @Test
    public void matchesByDueDate() {
        LocalDate due = LocalDate.of(2025, 5, 20);
        Task t = new BasicTask(3L, "Pay bills", null, new CompletedState(), due);
        assertTrue(TaskFilter.byDueDate(due).matches(t));
        assertFalse(TaskFilter.byDueDate(due.plusDays(1)).matches(t));
    }

    @Test
    public void andOrCombinators() {
        Task a = new BasicTask(4L, "Alpha", null, new ToDoState(), null);
        Task b = new BasicTask(5L, "Beta", null, new ToDoState(), null);

        TaskFilter titleA = TaskFilter.byTitle("Alpha");
        TaskFilter stateTodo = TaskFilter.byState("TODO");

        assertTrue(titleA.and(stateTodo).matches(a));
        assertFalse(titleA.and(stateTodo).matches(b));

        TaskFilter titleB = TaskFilter.byTitle("Beta");
        assertTrue(titleA.or(titleB).matches(a));
        assertTrue(titleA.or(titleB).matches(b));
    }
}









