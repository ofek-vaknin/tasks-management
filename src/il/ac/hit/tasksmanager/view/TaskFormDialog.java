package il.ac.hit.tasksmanager.view;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import il.ac.hit.tasksmanager.model.entities.state.ToDoState;
import il.ac.hit.tasksmanager.model.entities.state.InProgressState;
import il.ac.hit.tasksmanager.model.entities.state.CompletedState;
import il.ac.hit.tasksmanager.view.dto.TaskInput;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * TaskFormDialog is a reusable modal dialog for creating or editing tasks.
 * It validates title, due date (not in the past), and requires interval
 * when the task type is RECURRING.
 */
public class TaskFormDialog extends JDialog {
    private final JTextField titleField = new JTextField(20);
    private final JTextField descField = new JTextField(30);
    private final JComboBox<String> stateCombo = new JComboBox<>(new String[]{"TODO", "IN_PROGRESS", "COMPLETED"});
    private final JTextField dueDateField = new JTextField(12);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"BASIC", "RECURRING"});
    private final JLabel intervalLabel = new JLabel("Interval (days):");
    private final JTextField intervalField = new JTextField(6);

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
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0; gbc.gridx = 0;
        form.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(titleField, gbc);

        gbc.gridy = 1; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(descField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        form.add(new JLabel("State:"), gbc);
        gbc.gridx = 1;
        form.add(stateCombo, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("Due (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(dueDateField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        form.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        form.add(typeCombo, gbc);

        gbc.gridy = 5; gbc.gridx = 0;
        form.add(intervalLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(intervalField, gbc);
        add(form, BorderLayout.CENTER);

        intervalLabel.setVisible(false);
        intervalField.setVisible(false);
        typeCombo.addActionListener(e -> {
            boolean isRecurring = "RECURRING".equals(typeCombo.getSelectedItem());
            intervalLabel.setVisible(isRecurring);
            intervalField.setVisible(isRecurring);
        });

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
     * Updates UI controls to reflect the given task's fields (state, type, due date).
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
        if (task instanceof il.ac.hit.tasksmanager.model.RecurringTask r) {
            typeCombo.setSelectedItem("RECURRING");
            intervalField.setText(Integer.toString(r.interval()));
            intervalLabel.setVisible(true);
            intervalField.setVisible(true);
        } else {
            typeCombo.setSelectedItem("BASIC");
            intervalField.setText("");
            intervalLabel.setVisible(false);
            intervalField.setVisible(false);
        }
    }

    private TaskInput validateAndBuild() {
        /*
         * Validation + DTO build flow:
         * - Read fields (title, description, state, due)
         * - Validate title non-empty and due date not in the past
         * - If type is RECURRING, validate positive integer interval
         * - Build immutable TaskInput DTO for the caller (MainWindow)
         */
        try {
            String title = titleField.getText();
            String desc = descField.getText();
            TaskState st = parseState((String) stateCombo.getSelectedItem());
            LocalDate due = null;
            if (!dueDateField.getText().trim().isEmpty()) {
                due = LocalDate.parse(dueDateField.getText().trim());
            }
            if (due != null && due.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Due date must be today or in the future");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
            int recDays = 0;
            boolean isRecurring = "RECURRING".equals(typeCombo.getSelectedItem());
            if (isRecurring) {
                String txt = intervalField.getText() == null ? "" : intervalField.getText().trim();
                if (txt.isEmpty()) {
                    throw new IllegalArgumentException("Interval is required for RECURRING tasks");
                }
                int days;
                try {
                    days = Integer.parseInt(txt);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Interval must be a positive integer");
                }
                if (days <= 0) {
                    throw new IllegalArgumentException("Interval must be a positive integer");
                }
                recDays = days;
            }
            return new TaskInput(title.trim(), desc == null ? "" : desc.trim(), st, due, recDays);
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

    // removed legacy recurrence pattern helpers (replaced by explicit Type + Interval)
}


