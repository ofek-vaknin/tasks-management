package il.ac.hit.tasksmanager.model.visitor;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.RecurringTask;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import java.time.LocalDate;

/**
 * GUIReportVisitor implements the Visitor pattern using Java records and
 * pattern matching over the sealed hierarchy of {@link Task} records.
 */
public final class GUIReportVisitor {
    /**
     * Formats a task into a single textual line for GUI reports.
     */
    public String format(Task task) {
        /*
         * Visitor (record pattern matching)
         * - Select concrete record by deconstructing patterns inside a switch
         * - Avoids classic double-dispatch by leveraging sealed interface + records
         * - Each case extracts fields directly and composes the output string
         */
        return switch (task) {
            case BasicTask(int id, String title, String description, TaskState state, LocalDate dueDate) ->
                String.format("[%d] %s [%s] - %s (Type: BASIC)", id, title, state, dueDate);
            case RecurringTask(int id, String title, String description, TaskState state, LocalDate dueDate, int interval) ->
                String.format("[%d] %s [%s] - %s (Type: RECURRING, Interval: %d days)", id, title, state, dueDate, interval);
        };
    }
}


