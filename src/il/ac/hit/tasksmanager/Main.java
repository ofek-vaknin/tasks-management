package il.ac.hit.tasksmanager;

import il.ac.hit.tasksmanager.model.ModelException;
import il.ac.hit.tasksmanager.view.MainWindow;
import il.ac.hit.tasksmanager.viewmodel.TasksListViewModel;

import javax.swing.SwingUtilities;

/**
 * App entry point. Launches the Swing UI on the EDT and wires the view model to the main window.
 */
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			// Launch on Event Dispatch Thread
			try {
				TasksListViewModel vm = new TasksListViewModel(); // ViewModel wiring
				MainWindow window = new MainWindow(vm); // Main UI window
				window.setVisible(true);
			} catch (ModelException e) {
				e.printStackTrace();
			}
		});
	}
}


