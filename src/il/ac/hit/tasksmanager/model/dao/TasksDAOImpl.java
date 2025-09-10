package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.RecurringTask;
import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.entities.ITask;
import il.ac.hit.tasksmanager.model.entities.state.CompletedState;
import il.ac.hit.tasksmanager.model.entities.state.InProgressState;
import il.ac.hit.tasksmanager.model.entities.state.TaskState;
import il.ac.hit.tasksmanager.model.entities.state.ToDoState;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * TasksDAOImpl is a Derby-backed DAO (Singleton) that persists tasks.
 *
 * DAO Pattern: encapsulates all persistence concerns and exposes
 * a small CRUD API defined by {@link ITasksDAO}.
 */
public class TasksDAOImpl implements ITasksDAO {
	private static final String DB_URL = "jdbc:derby:tasksdb;create=true";
	private static TasksDAOImpl instance;

	/**
	 * Creates the DAO and ensures the database schema exists.
	 *
	 * @throws TasksDAOException when the embedded driver is missing or schema init fails
	 */
	private TasksDAOImpl() throws TasksDAOException {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			initSchema();
		} catch (ClassNotFoundException e) {
			throw new TasksDAOException("Derby EmbeddedDriver not found in classpath", e);
		}
	}

	/**
	 * Returns the singleton instance of this DAO (Singleton pattern).
	 *
	 * @return singleton instance
	 * @throws TasksDAOException when initialization fails
	 */
	public static synchronized TasksDAOImpl getInstance() throws TasksDAOException {
		if (instance == null) {
			instance = new TasksDAOImpl();
		}
		return instance;
	}

	/**
	 * Opens a new JDBC connection to the embedded Derby database.
	 *
	 * @return a JDBC connection
	 * @throws SQLException if the connection cannot be established
	 */
	private Connection getConnection() throws SQLException {
		/* new connection per op to avoid leaks */
		return DriverManager.getConnection(DB_URL);
	}

	/**
	 * Ensures the required TASKS table and columns exist.
	 * Creates the table when missing and adds columns when the table exists.
	 *
	 * @throws TasksDAOException on schema initialization failure
	 */
	private void initSchema() throws TasksDAOException {
		// schema bootstrap
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			try {
				stmt.executeUpdate(
					"CREATE TABLE TASKS (" +
					"ID INT PRIMARY KEY, " +
					"TITLE VARCHAR(255) NOT NULL, " +
					"DESCRIPTION VARCHAR(1024), " +
					"STATE VARCHAR(32) NOT NULL, " +
					"DUEDATE DATE, " +
					"RECURRENCE_DAYS INT)"
				);
			} catch (SQLException e) {
				// table exists (X0Y32) → ignore
				if (!"X0Y32".equals(e.getSQLState())) {
					throw e;
				}
			}
			// attempt additive DDL (idempotent)
			try { stmt.executeUpdate("ALTER TABLE TASKS ADD COLUMN DUEDATE DATE"); } catch (SQLException ignore) { }
			try { stmt.executeUpdate("ALTER TABLE TASKS ADD COLUMN RECURRENCE_DAYS INT"); } catch (SQLException ignore) { }
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to initialize schema", e);
		}
	}

	/**
	 * Computes the next unique identifier by selecting MAX(ID)+1.
	 * Returns 1 when the table is empty.
	 *
	 * @return next id value
	 * @throws TasksDAOException when the query fails
	 */
	private int nextId() throws TasksDAOException {
		// id generation via MAX(ID)+1
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(ID) FROM TASKS")) {
			if (rs.next()) {
				int max = rs.getInt(1);
				if (rs.wasNull()) {
					return 1;
				}
				return max + 1;
			}
			return 1;
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to generate next ID", e);
		}
	}

	/**
	 * Inserts a new task into the TASKS table.
	 *
	 * @param task task to persist
	 * @throws TasksDAOException when the insert fails
	 */
    @Override
	public void addTask(ITask task) throws TasksDAOException {
		if (task == null) {
			throw new TasksDAOException("task must not be null");
		}
		// insert row
		int id = nextId();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO TASKS (ID, TITLE, DESCRIPTION, STATE, DUEDATE, RECURRENCE_DAYS) VALUES (?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, id);
			ps.setString(2, task.getTitle());
			ps.setString(3, task.getDescription());
			ps.setString(4, task.getState().name());
			java.time.LocalDate due = null;
			int recurrenceDays = 0;
			if (task instanceof Task mt) {
				due = mt.dueDate();
				if (mt instanceof RecurringTask rtask) { recurrenceDays = rtask.interval(); }
			}
			if (due != null) { ps.setDate(5, Date.valueOf(due)); } else { ps.setNull(5, Types.DATE); }
			if (recurrenceDays > 0) { ps.setInt(6, recurrenceDays); } else { ps.setNull(6, Types.INTEGER); }
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to add task", e);
		}
	}

	/**
	 * Reads all tasks ordered by ID and maps each row into a Task record.
	 *
	 * @return array of tasks (possibly empty)
	 * @throws TasksDAOException when the query fails
	 */
    @Override
	public ITask[] getTasks() throws TasksDAOException {
		List<Task> tasks = new ArrayList<>();
		// query and map rows
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ID, TITLE, DESCRIPTION, STATE, DUEDATE, RECURRENCE_DAYS FROM TASKS ORDER BY ID")) {
			while (rs.next()) {
				int id = rs.getInt("ID");
				String title = rs.getString("TITLE");
				String description = rs.getString("DESCRIPTION");
				String stateStr = rs.getString("STATE");
				Date dueSql = rs.getDate("DUEDATE");
				Integer recDays = (Integer) rs.getObject("RECURRENCE_DAYS");
				TaskState state = switch (stateStr) {
					case "TODO" -> new ToDoState();
					case "IN_PROGRESS" -> new InProgressState();
					case "COMPLETED" -> new CompletedState();
					default -> new ToDoState();
				};
				if (recDays != null && recDays > 0) {
					tasks.add(new RecurringTask(id, title, description, state, dueSql == null ? null : dueSql.toLocalDate(), recDays));
				} else {
					tasks.add(new BasicTask(id, title, description, state, dueSql == null ? null : dueSql.toLocalDate()));
				}
			}
			return tasks.toArray(new ITask[0]);
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to fetch tasks", e);
		}
	}

	/**
	 * Reads a single task by id.
	 *
	 * @param id identifier to look up
	 * @return the task if found, otherwise null
	 * @throws TasksDAOException when the query fails
	 */
    @Override
	public ITask getTask(int id) throws TasksDAOException {
		// validate
		if (id <= 0) { throw new TasksDAOException("id must be positive"); }
		// query single row
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT ID, TITLE, DESCRIPTION, STATE, DUEDATE, RECURRENCE_DAYS FROM TASKS WHERE ID = ?")) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String title = rs.getString("TITLE");
					String description = rs.getString("DESCRIPTION");
					String stateStr = rs.getString("STATE");
					Date dueSql = rs.getDate("DUEDATE");
					Integer recDays = (Integer) rs.getObject("RECURRENCE_DAYS");
					TaskState state = switch (stateStr) {
						case "TODO" -> new ToDoState();
						case "IN_PROGRESS" -> new InProgressState();
						case "COMPLETED" -> new CompletedState();
						default -> new ToDoState();
					};
					int rid = rs.getInt("ID");
					return (recDays != null && recDays > 0)
						? new RecurringTask(rid, title, description, state, dueSql == null ? null : dueSql.toLocalDate(), recDays)
						: new BasicTask(rid, title, description, state, dueSql == null ? null : dueSql.toLocalDate());
				}
				return null;
			}
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to get task id=" + id, e);
		}
	}

	/**
	 * Updates an existing task in the TASKS table.
	 *
	 * @param task task with new values
	 * @throws TasksDAOException when the update fails
	 */
    @Override
    public void updateTask(ITask task) throws TasksDAOException {
		// validate
		if (task == null) { throw new TasksDAOException("task must not be null"); }
		// perform update
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE TASKS SET TITLE = ?, DESCRIPTION = ?, STATE = ?, DUEDATE = ?, RECURRENCE_DAYS = ? WHERE ID = ?")) {
			ps.setString(1, task.getTitle());
			ps.setString(2, task.getDescription());
			ps.setString(3, task.getState().name());
			java.time.LocalDate due = null; int recurrenceDays = 0;
			if (task instanceof Task mt) { due = mt.dueDate(); if (mt instanceof RecurringTask rtask) { recurrenceDays = rtask.interval(); } }
			if (due != null) { ps.setDate(4, Date.valueOf(due)); } else { ps.setNull(4, Types.DATE); }
			if (recurrenceDays > 0) { ps.setInt(5, recurrenceDays); } else { ps.setNull(5, Types.INTEGER); }
			ps.setLong(6, task.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to update task id=" + task.getId(), e);
		}
	}

	/**
	 * Deletes a single task by id.
	 *
	 * @param id task id
	 * @throws TasksDAOException when the delete fails
	 */
    @Override
    public void deleteTask(int id) throws TasksDAOException {
		// validate and delete
		if (id <= 0) { throw new TasksDAOException("id must be positive"); }
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM TASKS WHERE ID = ?")) {
			ps.setLong(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to delete task id=" + id, e);
		}
	}

	/**
	 * Deletes all tasks from the TASKS table.
	 *
	 * @throws TasksDAOException when the delete fails
	 */
    @Override
    public void deleteTasks() throws TasksDAOException {
		// bulk delete
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DELETE FROM TASKS");
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to delete all tasks", e);
		}
	}
}


