package il.ac.hit.tasksmanager.model.observer;

/**
 * Observer pattern: Views/ViewModels implement this interface to be notified
 * by the Model (subject) when the tasks data set changes.
 * Where are observers located
 * - il.ac.hit.tasksmanager.viewmodel.TasksListViewModel implements this interface
 *   and acts as the primary observer of the Model.
 * - Views (Swing components) do NOT observe the Model directly; instead they observe
 *   the ViewModel via il.ac.hit.tasksmanager.viewmodel.observer.ViewModelObserver.
 * How to register/unregister
 * - Use il.ac.hit.tasksmanager.model.Model#register(TaskObserver) to subscribe.
 * - Use il.ac.hit.tasksmanager.model.Model#remove(TaskObserver) to unsubscribe.
 * When is it called
 * - After add/update/delete operations.
 * - After reloading tasks from the DAO (for example, at startup).
 *
 * @see il.ac.hit.tasksmanager.model.Model#register(TaskObserver)
 * @see il.ac.hit.tasksmanager.model.Model#remove(TaskObserver)
 * @see il.ac.hit.tasksmanager.viewmodel.TasksListViewModel
 */
@FunctionalInterface
public interface TaskObserver {
    /** Called when tasks have changed (add/update/delete or reload). */
    void onTasksChanged();
}
