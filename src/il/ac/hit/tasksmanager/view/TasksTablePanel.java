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
 * TasksTablePanel encapsulates the JTable that displays tasks.
 */
public class TasksTablePanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final JTable table;

    public TasksTablePanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "State", "Due Date"}, 0) {
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
        tableModel.setRowCount(0);
        for (Task t : tasks) {
            Vector<Object> row = new Vector<>();
            row.add(t.id());
            row.add(t.title());
            row.add(t.description() == null ? "" : t.description());
            row.add(t.state());
            row.add(t.dueDate() == null ? "" : t.dueDate());
            tableModel.addRow(row);
        }
    }

    public List<Long> getSelectedTaskIds() {
        int[] selected = table.getSelectedRows();
        List<Long> ids = new ArrayList<>();
        for (int viewRow : selected) {
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


