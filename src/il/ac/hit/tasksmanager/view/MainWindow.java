package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.TaskState;
import il.ac.hit.tasksmanager.viewmodel.TasksListViewModel;
import il.ac.hit.tasksmanager.viewmodel.observer.ViewModelObserver;
import il.ac.hit.tasksmanager.model.combinator.TaskFilter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * MainWindow is the primary Swing frame binding the view model to UI panels.
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
        top.add(filterPanel, BorderLayout.WEST);
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

    private void onAdd() {
        TaskFormDialog dlg = new TaskFormDialog(this);
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getTaskInput() != null) {
            TaskFormDialog.TaskInput in = dlg.getTaskInput();
            try {
                viewModel.addTask(in.title(), in.description(), in.state(), in.dueDate());
            } catch (il.ac.hit.tasksmanager.model.ModelException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEdit() {
        List<Long> ids = tablePanel.getSelectedTaskIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.");
            return;
        }
        long id = ids.get(0);
        Task existing = viewModel.getTasks().stream().filter(t -> t.id() == id).findFirst().orElse(null);
        TaskFormDialog dlg = new TaskFormDialog(this);
        if (existing != null) {
            dlg.setTask(existing);
        }
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getTaskInput() != null && existing != null) {
            TaskFormDialog.TaskInput in = dlg.getTaskInput();
            Task updated = new il.ac.hit.tasksmanager.model.BasicTask(existing.id(), in.title(), in.description(), in.state(), in.dueDate());
            try {
                viewModel.updateTask(updated);
            } catch (il.ac.hit.tasksmanager.model.ModelException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDelete() {
        List<Long> ids = tablePanel.getSelectedTaskIds();
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one task to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected tasks?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        for (Long id : ids) {
            try {
                viewModel.deleteTask(id);
            } catch (il.ac.hit.tasksmanager.model.ModelException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
        }
    }

    private void onReport() {
        StringBuilder sb = new StringBuilder();
        for (Task t : viewModel.getTasks()) {
            sb.append("#").append(t.id()).append(" ").append(t.title()).append(" [").append(t.state()).append("]\n");
        }
        JOptionPane.showMessageDialog(this, sb.length() == 0 ? "No tasks." : sb.toString(), "Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTable() {
        List<Task> tasks = currentFilter == null ? viewModel.getTasks() : viewModel.getFilteredTasks(currentFilter);
        tablePanel.setTasks(tasks);
    }

    @Override
    public void update() {
        refreshTable();
    }
}


