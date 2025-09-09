package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.Task;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.LocalDate;
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

    public TasksTablePanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "State", "Due Date", "Type", "Interval"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setTasks(List<Task> tasks) {
        /**
         * Rebuild the table model from the provided list of tasks.
         */
        tableModel.setRowCount(0);
        for (Task t : tasks) {
            // Build a row per task
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

    public List<Long> getSelectedTaskIds() {
        /**
         * Translate the selected rows into a list of task IDs.
         */
        int[] selected = table.getSelectedRows();
        List<Long> ids = new ArrayList<>();
        for (int viewRow : selected) {
            // Convert view index to model index (sorting may be active)
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object idObj = tableModel.getValueAt(modelRow, 0);
            if (idObj instanceof Number) {
                ids.add(((Number) idObj).longValue());
            } else if (idObj != null) {
                try {
                    ids.add(Long.parseLong(idObj.toString()));
                } catch (NumberFormatException ignored) { }
            }
        }
        return ids;
    }
}


