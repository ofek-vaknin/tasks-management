package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.combinator.TaskFilter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/**
 * FilterPanel collects user input for task filtering (title, state, due date)
 * and emits a composed {@link il.ac.hit.tasksmanager.model.combinator.TaskFilter}
 * using AND/OR composition.
 */
public class FilterPanel extends JPanel {
	private final JTextField titleField = new JTextField(15);
	private final JComboBox<String> stateCombo = new JComboBox<>(new String[]{"", "TODO", "IN_PROGRESS", "COMPLETED"});
	private final JTextField dueField = new JTextField(10);
	private final JComboBox<String> combiner = new JComboBox<>(new String[]{"AND", "OR"});

	/**
	 * Creates a filter panel.
	 *
	 * @param onApplyFilter callback that receives the composed TaskFilter when the user clicks Apply
	 */
	public FilterPanel(Consumer<TaskFilter> onApplyFilter) {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridy = 0;
		gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
		add(new JLabel("Title:"), gbc);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
		add(titleField, gbc);
		gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
		add(new JLabel("State:"), gbc);
		gbc.gridx = 3;
		add(stateCombo, gbc);

		gbc.gridy = 1; gbc.gridx = 0;
		add(new JLabel("Due (YYYY-MM-DD):"), gbc);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
		add(dueField, gbc);
		gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
		add(new JLabel("Combine:"), gbc);
		gbc.gridx = 3;
		add(combiner, gbc);

		gbc.gridx = 4; gbc.anchor = GridBagConstraints.EAST;
		JButton apply = new JButton("Apply Filter");
		add(apply, gbc);

		apply.addActionListener(e -> {
			/**
			 * Build a TaskFilter using AND/OR composition per user selection.
			 */
			String title = titleField.getText() == null ? "" : titleField.getText();
			String state = (String) stateCombo.getSelectedItem();
			String dueText = dueField.getText() == null ? "" : dueField.getText().trim();
			boolean useOr = "OR".equals(combiner.getSelectedItem());

			TaskFilter titleFilter = TaskFilter.byTitle(title);
			TaskFilter stateFilter = (state != null && !state.isEmpty()) ? TaskFilter.byState(state) : null;
			TaskFilter dueFilter = null;
			if (!dueText.isEmpty()) {
				try {
					java.time.LocalDate date = java.time.LocalDate.parse(dueText);
					dueFilter = TaskFilter.byDueDate(date);
				} catch (java.time.format.DateTimeParseException ignore) { /* ignore invalid date; treat as not provided */ }
			}

			TaskFilter filter = titleFilter;
			if (stateFilter != null) {
				filter = useOr ? filter.or(stateFilter) : filter.and(stateFilter);
			}
			if (dueFilter != null) {
				filter = useOr ? filter.or(dueFilter) : filter.and(dueFilter);
			}
			onApplyFilter.accept(filter);
		});
	}
}


