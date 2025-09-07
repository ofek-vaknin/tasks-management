package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.entities.TaskState;
import java.time.LocalDate;

/**
 * BasicTask is a simple immutable task record used by the app.
 */
public record BasicTask(long id, String title, String description, TaskState state, LocalDate dueDate) implements Task {}


