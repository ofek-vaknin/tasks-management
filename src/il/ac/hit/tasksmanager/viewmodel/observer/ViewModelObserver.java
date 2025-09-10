package il.ac.hit.tasksmanager.viewmodel.observer;

/**
 * Observer interface on the View side for ViewModel updates.
 * Views implement this interface and register with the ViewModel to be
 * notified when the tasks data changes and the UI should refresh.
 * Typical implementors: {@code il.ac.hit.tasksmanager.view.MainWindow}
 */
public interface ViewModelObserver {
	/** Called when the view model's data changes and the view should refresh. */
	void update();
}


