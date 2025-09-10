package il.ac.hit.tasksmanager.model;

import il.ac.hit.tasksmanager.model.entities.ITask;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import java.time.LocalDate;

/**
 * BasicTask is an immutable domain record that represents a single task.
 * Validation is performed in the canonical constructor.
 */
public record BasicTask(int id, String title, String description, TaskState state, LocalDate dueDate)
        implements Task, ITask {

    /**
     * Canonical constructor with basic validations.
     * Validation: ensure required fields are present.
     */
    public BasicTask {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
    }

    @Override
    public int getId() {
        return id; }

    @Override
    public String getTitle() {
        return title; }

    @Override
    public String getDescription() {
        return description; }

    @Override
    public TaskState getState() {
        return state; }
}
