package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.Task;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * TasksTablePanel encapsulates a JTable for displaying tasks with columns
 * for ID, Title, Description, State, Due Date, Type and Interval.
 */
public class TasksTablePanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final JTable table;

    /** Creates the table panel and initializes the table model and sorter. */
    public TasksTablePanel() {
        setLayout(new BorderLayout());
        // build table model
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "State", "Due Date", "Type", "Interval"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // create table and enable sorting
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /**
     * Rebuilds the table model from the provided list of tasks.
     * @param tasks tasks to display
     */
    public void setTasks(List<Task> tasks) {
        // clear table
        tableModel.setRowCount(0);
        // add rows
        for (Task t : tasks) {
            Vector<Object> row = new Vector<>();
            row.add(t.id());
            row.add(t.title());
            row.add(t.description() == null ? "" : t.description());
            row.add(t.state());
            row.add(t.dueDate() == null ? "" : t.dueDate());
            if (t instanceof il.ac.hit.tasksmanager.model.RecurringTask r) {
                row.add("RECURRING");
                row.add(r.interval());
            } else {
                row.add("BASIC");
                row.add("");
            }
            tableModel.addRow(row);
        }
    }

    /**
     * Translates the selected rows into a list of task IDs (int).
     * @return list of selected task identifiers
     */
    public List<Integer> getSelectedTaskIds() {
        // gather selected rows
        int[] selected = table.getSelectedRows();
        List<Integer> ids = new ArrayList<>();
        for (int viewRow : selected) {
            // map view index to model index
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object idObj = tableModel.getValueAt(modelRow, 0);
            if (idObj instanceof Number) {
                ids.add(((Number) idObj).intValue());
            } else if (idObj != null) {
                try {
                    ids.add(Integer.parseInt(idObj.toString()));
                } catch (NumberFormatException ignored) { }
            }
        }
        return ids;
    }
}


