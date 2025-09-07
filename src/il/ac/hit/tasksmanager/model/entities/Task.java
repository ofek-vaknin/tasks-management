package il.ac.hit.tasksmanager.model.entities;

public record Task(long id, String title, String description, TaskState state) implements ITask {}


