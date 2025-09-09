package il.ac.hit.tasksmanager.model.combinator;

import il.ac.hit.tasksmanager.model.Task;
import java.time.LocalDate;

/**
 * TaskFilter implements the Combinator pattern for flexible search.
 * It composes small predicate-like filters with AND/OR to create
 * rich filtering logic at runtime.
 */
@FunctionalInterface
public interface TaskFilter {
    /**
     * Determines whether the given task satisfies the filter condition.
     *
     * @param task the task to check
     * @return true if the task matches the filter, false otherwise
     */

    boolean matches(Task task);
    /**
     * Logical AND of two filters.
     * Combinator: function composition.
     *
     * @param other another filter to combine with this one
     * @return a new filter that matches only if both filters match
     */
    default TaskFilter and(TaskFilter other) {
        /*
         * Combines this filter with another using logical AND.
         * This represents the essence of the Combinator pattern,
         * composing small functions into larger behavior.
         */
        return task -> this.matches(task) && other.matches(task);
    }

    /**
     * Logical OR of two filters.
     * Combinator: function composition.
     *
     * @param other another filter to combine with this one
     * @return a new filter that matches if either filter matches
     */
    default TaskFilter or(TaskFilter other) {
        /*
         * Combines this filter with another using logical OR.
         * Allows flexible filter chaining where multiple criteria may apply.
         */
        return task -> this.matches(task) || other.matches(task);
    }

    /**
     * Matches tasks whose title contains the given text (case-insensitive).
     *
     * @param title the text to search for in the task title
     * @return a TaskFilter that matches tasks with matching title
     */
    static TaskFilter byTitle(String title) {
        /*
         * Returns a filter that checks if the task title contains the given string.
         * Useful for keyword search functionality.
         */
        return task -> task.title() != null && task.title().toLowerCase().contains(title.toLowerCase());
    }

    /**
     * Matches tasks by state name.
     *
     * @param stateName the name of the state to match
     * @return a TaskFilter that matches tasks with the given state
     */
    static TaskFilter byState(String stateName) {
        /*
         * Returns a filter that checks if the task is in a specific state.
         * Enables filtering by workflow or status.
         */
        return task -> task.state() != null && task.state().name().equalsIgnoreCase(stateName);
    }

    /**
     * Matches tasks with the due date equal to the given date.
     *
     * @param date the due date to match
     * @return a TaskFilter that matches tasks due on the given date
     */
    static TaskFilter byDueDate(LocalDate date) {
        /*
         * Returns a filter that checks if the task is due on a specific date.
         * Useful for time-based filtering or scheduling views.
         */
        return task -> task.dueDate() != null && task.dueDate().equals(date);
    }
}
