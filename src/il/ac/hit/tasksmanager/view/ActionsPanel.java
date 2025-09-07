package il.ac.hit.tasksmanager.view;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;

/**
 * ActionsPanel presents the primary actions for tasks: Add, Edit, Delete, Refresh, and Generate Report.
 */
public class ActionsPanel extends JPanel {
    public ActionsPanel(Runnable onAdd, Runnable onEdit, Runnable onDelete, Runnable onRefresh, Runnable onReport) {
        // === Build actions bar ===
        setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");
        JButton reportBtn = new JButton("Generate Report");
        add(addBtn);
        add(editBtn);
        add(delBtn);
        add(refreshBtn);
        add(reportBtn);
        addBtn.addActionListener(e -> onAdd.run());
        editBtn.addActionListener(e -> onEdit.run());
        delBtn.addActionListener(e -> onDelete.run());
        refreshBtn.addActionListener(e -> onRefresh.run());
        reportBtn.addActionListener(e -> onReport.run());
    }
}


