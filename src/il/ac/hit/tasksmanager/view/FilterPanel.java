package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.combinator.TaskFilter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.util.function.Consumer;

/**
 * FilterPanel collects user input for task filtering (title and state) and emits a composed filter.
 */
public class FilterPanel extends JPanel {
	private final JTextField titleField = new JTextField(15);
	private final JComboBox<String> stateCombo = new JComboBox<>(new String[]{"", "TODO", "IN_PROGRESS", "COMPLETED"});

	/**
	 * Creates a filter panel.
	 *
	 * @param onApplyFilter callback that receives the composed TaskFilter when the user clicks Apply
	 */
	public FilterPanel(Consumer<TaskFilter> onApplyFilter) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
		add(new JLabel("Title:"));
		add(titleField);
		add(new JLabel("State:"));
		add(stateCombo);
		JButton apply = new JButton("Apply Filter");
		add(apply);

		apply.addActionListener(e -> {
			String title = titleField.getText() == null ? "" : titleField.getText();
			String state = (String) stateCombo.getSelectedItem();
			TaskFilter filter = TaskFilter.byTitle(title);
			if (state != null && !state.isEmpty()) {
				filter = filter.and(TaskFilter.byState(state));
			}
			onApplyFilter.accept(filter);
		});
	}
}


