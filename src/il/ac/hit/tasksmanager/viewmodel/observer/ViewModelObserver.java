package il.ac.hit.tasksmanager.viewmodel.observer;

/**
 * ViewModelObserver is notified by the view model when tasks have changed.
 */
public interface ViewModelObserver {
	/** Called when the view model's data changes and the view should refresh. */
	void update();
}


