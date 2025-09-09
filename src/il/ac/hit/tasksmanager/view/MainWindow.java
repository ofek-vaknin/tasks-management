package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.RecurringTask;
import il.ac.hit.tasksmanager.model.ModelException;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import il.ac.hit.tasksmanager.viewmodel.TasksListViewModel;
import il.ac.hit.tasksmanager.viewmodel.observer.ViewModelObserver;
import il.ac.hit.tasksmanager.model.combinator.TaskFilter;
import il.ac.hit.tasksmanager.model.visitor.GUIReportVisitor;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * MainWindow is the primary Swing frame that binds the view model to
 * UI panels (filters, actions, and table). All UI interactions occur
 * on the EDT.
 */
public class MainWindow extends JFrame implements ViewModelObserver {
    private final TasksListViewModel viewModel;
    private final TasksTablePanel tablePanel = new TasksTablePanel();
    private final FilterPanel filterPanel;

    private TaskFilter currentFilter = task -> true;

    public MainWindow(TasksListViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.registerObserver(this);

        setTitle("Tasks Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        filterPanel = new FilterPanel(filter -> {
            currentFilter = filter;
            refreshTable();
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(filterPanel, BorderLayout.CENTER);
        top.add(new ActionsPanel(
                this::onAdd,
                this::onEdit,
                this::onDelete,
                this::refreshTable,
                this::onReport
        ), BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        pack();
        setMinimumSize(new java.awt.Dimension(1000, 700));
        setSize(1100, 750);
        setLocationRelativeTo(null);
        refreshTable();
    }

    /**
     * Handles the Add action by opening the form dialog and delegating creation
     * to the view model according to the user's input.
     */
    private void onAdd() {
        /**
         * Open a modal dialog to collect task input and delegate add to the view model.
         */
        TaskFormDialog dlg = new TaskFormDialog(this);
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getTaskInput() != null) {
            TaskFormDialog.TaskInput in = dlg.getTaskInput();
            try {
                if (in.recurrenceDays() > 0) {
                    viewModel.addRecurringTask(in.title(), in.description(), in.state(), in.dueDate(), in.recurrenceDays());
                } else {
                    viewModel.addTask(in.title(), in.description(), in.state(), in.dueDate());
                }
            } catch (ModelException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the Edit action by editing the first selected task and delegating
     * the update to the view model.
     */
    private void onEdit() {
        /**
         * Edit the first selected task. We read the selection from the table,
         * show the dialog pre-populated, and forward update to the view model.
         */
        List<Long> ids = tablePanel.getSelectedTaskIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.");
            return;
        }
        int id = ids.get(0).intValue();
        Task existing = viewModel.getTasks().stream().filter(t -> t.id() == id).findFirst().orElse(null);
        TaskFormDialog dlg = new TaskFormDialog(this);
        if (existing != null) {
            dlg.setTask(existing);
        }
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getTaskInput() != null && existing != null) {
            TaskFormDialog.TaskInput in = dlg.getTaskInput();
            Task updated;
            if (in.recurrenceDays() > 0) {
                updated = new RecurringTask(existing.id(), in.title(), in.description(), in.state(), in.dueDate(), in.recurrenceDays());
            } else {
                updated = new BasicTask(existing.id(), in.title(), in.description(), in.state(), in.dueDate());
            }
            try {
                viewModel.updateTask(updated);
            } catch (ModelException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the Delete action by removing all selected tasks after user confirmation.
     */
    private void onDelete() {
        /**
         * Delete all selected tasks after confirmation by delegating to the view model.
         */
        List<Long> ids = tablePanel.getSelectedTaskIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one task to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected tasks?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        for (Long id : ids) {
            try {
                viewModel.deleteTask(id.intValue());
            } catch (ModelException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
        }
    }

    /**
     * Generates and displays a simple textual report of all tasks using the Visitor.
     */
    private void onReport() {
        /**
         * Compose a simple report by visiting each task and showing the output.
         */
        GUIReportVisitor visitor = new GUIReportVisitor();
        StringBuilder sb = new StringBuilder();
        for (Task t : viewModel.getTasks()) {
            // Visit each task for formatting
            sb.append(visitor.format(t)).append("\n");
        }
        String report = sb.length() == 0 ? "No tasks." : sb.toString();
        JOptionPane.showMessageDialog(this, report, "Task Report", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Refreshes the table data based on the current filter (Combinator predicate).
     */
    private void refreshTable() {
        /**
         * Refresh the table according to the current filter (combinator-based predicate).
         */
        List<Task> tasks = currentFilter == null ? viewModel.getTasks() : viewModel.getFilteredTasks(currentFilter);
        tablePanel.setTasks(tasks);
    }

    @Override
    public void update() {
        refreshTable();
    }
}


