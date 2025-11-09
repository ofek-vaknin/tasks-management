package il.ac.hit.tasksmanager.model.entities.state;

/**
 * Interface representing the state of a task (State design pattern).
 * Each Task object in the domain model holds a reference to a TaskState
 * that describes its current lifecycle phase.
 * Implementations provided in the application:
 * - ToDoState – a task that has not started
 * - InProgressState – a task currently being worked on
 * - CompletedState – a task that was finished
 * To add a new lifecycle phase, create another class that implements TaskState.
 * The textual identifier of each state is provided by the method name().
 * This identifier is used for:
 * - Filtering (for example, using combinators that check state names)
 * - Persistence (mapping states to and from the database)
 * - UI rendering and reporting
 */
public interface TaskState {

    /**
     * Returns a stable symbolic name for this state.
     * By default, this method delegates to toString().
     * Therefore, concrete state classes should override toString()
     * to supply a canonical name such as "TODO", "IN_PROGRESS", or "COMPLETED".
     *
     * @return the canonical and stable name of this state
     */
    default String name() {
        return toString();
    }
}