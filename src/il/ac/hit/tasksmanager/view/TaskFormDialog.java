package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.TaskState;
import il.ac.hit.tasksmanager.model.entities.ToDoState;
import il.ac.hit.tasksmanager.model.entities.InProgressState;
import il.ac.hit.tasksmanager.model.entities.CompletedState;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * TaskFormDialog is a reusable modal dialog for creating or editing tasks with validation.
 */
public class TaskFormDialog extends JDialog {
    private final JTextField titleField = new JTextField(20);
    private final JTextField descField = new JTextField(30);
    private final JComboBox<String> stateCombo = new JComboBox<>(new String[]{"TODO", "IN_PROGRESS", "COMPLETED"});
    private final JTextField dueDateField = new JTextField(12);

    private boolean confirmed = false;
    private TaskInput taskInput;

    /**
     * Creates a modal task form dialog.
     *
     * @param owner parent frame
     */
    public TaskFormDialog(Frame owner) {
        super(owner, true);
        setTitle("Task Details");
        setLayout(new BorderLayout(8, 8));
        // === Build form ===
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Description:"));
        form.add(descField);
        form.add(new JLabel("State:"));
        form.add(stateCombo);
        form.add(new JLabel("Due (YYYY-MM-DD):"));
        form.add(dueDateField);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttons.add(okButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            TaskInput input = validateAndBuild();
            if (input != null) {
                taskInput = input;
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            taskInput = null;
            dispose();
        });

        getRootPane().setDefaultButton(okButton);
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                taskInput = null;
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Pre-populates the dialog with an existing task.
     *
     * @param task existing task (nullable)
     */
    public void setTask(Task task) {
        if (task == null) return;
        titleField.setText(task.title());
        descField.setText("");
        String s = task.state() == null ? "TODO" : task.state().toString();
        stateCombo.setSelectedItem(s);
        dueDateField.setText(task.dueDate() == null ? "" : task.dueDate().toString());
    }

    private TaskInput validateAndBuild() {
        try {
            String title = titleField.getText();
            String desc = descField.getText();
            TaskState st = parseState((String) stateCombo.getSelectedItem());
            LocalDate due = null;
            if (!dueDateField.getText().trim().isEmpty()) {
                due = LocalDate.parse(dueDateField.getText().trim());
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
            return new TaskInput(title.trim(), desc == null ? "" : desc.trim(), st, due);
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Returns true if the user committed with OK. */
    public boolean isConfirmed() {
        return confirmed;
    }

    /** Returns the built TaskInput after confirmation, or null if canceled. */
    public TaskInput getTaskInput() {
        return taskInput;
    }

    private TaskState parseState(String value) {
        if (value == null) return new ToDoState();
        switch (value.toUpperCase()) {
            case "TODO":
                return new ToDoState();
            case "IN_PROGRESS":
                return new InProgressState();
            case "COMPLETED":
                return new CompletedState();
            default:
                return new ToDoState();
        }
    }

    public record TaskInput(String title, String description, TaskState state, LocalDate dueDate) {}
}


