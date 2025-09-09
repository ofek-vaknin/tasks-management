package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.RecurringTask;
import il.ac.hit.tasksmanager.model.Task;
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
		/*
		 * Open a short-lived connection for a single operation to keep the
		 * DAO stateless and avoid connection leaks.
		 */
		return DriverManager.getConnection(DB_URL);
	}

	/**
	 * Ensures the required TASKS table and columns exist.
	 * Creates the table when missing and adds columns when the table exists.
	 *
	 * @throws TasksDAOException on schema initialization failure
	 */
	private void initSchema() throws TasksDAOException {
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
				/*
				 * Derby error X0Y32 indicates the table already exists. Any other error
				 * should be rethrown to be handled by the caller.
				 */
				if (!"X0Y32".equals(e.getSQLState())) {
					throw e;
				}
			}
			try {
				stmt.executeUpdate("ALTER TABLE TASKS ADD COLUMN DUEDATE DATE");
			} catch (SQLException ignore) { }
			try {
				stmt.executeUpdate("ALTER TABLE TASKS ADD COLUMN RECURRENCE_DAYS INT");
			} catch (SQLException ignore) { }
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

	@Override
	/**
	 * Inserts a new task into the TASKS table.
	 *
	 * @param task task to persist
	 * @return the created task instance with generated id
	 * @throws TasksDAOException when the insert fails
	 */
	public Task addTask(Task task) throws TasksDAOException {
		// Validate input
		if (task == null) {
			throw new TasksDAOException("task must not be null");
		}
		Task toInsert = task;
		int id = nextId();
		// Open connection and prepare insert
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO TASKS (ID, TITLE, DESCRIPTION, STATE, DUEDATE, RECURRENCE_DAYS) VALUES (?, ?, ?, ?, ?, ?)")) {
			/*
			 * Map the Task record into relational columns. For recurring tasks we
			 * store the interval (in days) in RECURRENCE_DAYS; otherwise the column is NULL.
			 */
			ps.setInt(1, id);
			ps.setString(2, toInsert.title());
			ps.setString(3, toInsert.description());
			ps.setString(4, toInsert.state().name());
			if (toInsert.dueDate() != null) {
				ps.setDate(5, Date.valueOf(toInsert.dueDate()));
			} else {
				ps.setNull(5, Types.DATE);
			}
			int recurrenceDays = 0;
			if (toInsert instanceof RecurringTask rtask) {
				recurrenceDays = rtask.interval();
			}
			if (recurrenceDays > 0) {
				ps.setInt(6, recurrenceDays);
			} else {
				ps.setNull(6, Types.INTEGER);
			}
			// Execute insert
			ps.executeUpdate();
			if (toInsert instanceof BasicTask b) {
				return new BasicTask(id, b.title(), b.description(), b.state(), b.dueDate());
			} else if (toInsert instanceof RecurringTask r) {
				return new RecurringTask(id, r.title(), r.description(), r.state(), r.dueDate(), r.interval());
			}
			return toInsert;
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to add task", e);
		}
	}

	@Override
	/**
	 * Reads all tasks ordered by ID and maps each row into a Task record.
	 *
	 * @return array of tasks (possibly empty)
	 * @throws TasksDAOException when the query fails
	 */
	public Task[] getTasks() throws TasksDAOException {
		List<Task> tasks = new ArrayList<>();
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ID, TITLE, DESCRIPTION, STATE, DUEDATE, RECURRENCE_DAYS FROM TASKS ORDER BY ID")) {
			/* Map each result row to a BasicTask or RecurringTask depending on RECURRENCE_DAYS. */
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
			return tasks.toArray(new Task[0]);
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to fetch tasks", e);
		}
	}

	@Override
	/**
	 * Reads a single task by id.
	 *
	 * @param id identifier to look up
	 * @return the task if found, otherwise null
	 * @throws TasksDAOException when the query fails
	 */
	public Task getTask(int id) throws TasksDAOException {
		// Validate parameters
		if (id <= 0) {
			throw new TasksDAOException("id must be positive");
		}
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT ID, TITLE, DESCRIPTION, STATE, DUEDATE, RECURRENCE_DAYS FROM TASKS WHERE ID = ?")) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				/* Map a single row into a domain record or return null when not present. */
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
					if (recDays != null && recDays > 0) {
						return new RecurringTask(rid, title, description, state, dueSql == null ? null : dueSql.toLocalDate(), recDays);
					} else {
						return new BasicTask(rid, title, description, state, dueSql == null ? null : dueSql.toLocalDate());
					}
				}
				return null;
			}
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to get task id=" + id, e);
		}
	}

	@Override
	/**
	 * Updates an existing task in the TASKS table.
	 *
	 * @param task task with new values
	 * @throws TasksDAOException when the update fails
	 */
	public void updateTask(Task task) throws TasksDAOException {
		// Validate input
		if (task == null) {
			throw new TasksDAOException("task must not be null");
		}
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE TASKS SET TITLE = ?, DESCRIPTION = ?, STATE = ?, DUEDATE = ?, RECURRENCE_DAYS = ? WHERE ID = ?")) {
			/* Persist new scalar values; for non-recurring tasks set RECURRENCE_DAYS to NULL. */
			ps.setString(1, task.title());
			ps.setString(2, task.description());
			ps.setString(3, task.state().name());
			if (task.dueDate() != null) {
				ps.setDate(4, Date.valueOf(task.dueDate()));
			} else {
				ps.setNull(4, Types.DATE);
			}
			int recurrenceDays = 0;
			if (task instanceof RecurringTask rtask) {
				recurrenceDays = rtask.interval();
			}
			if (recurrenceDays > 0) {
				ps.setInt(5, recurrenceDays);
			} else {
				ps.setNull(5, Types.INTEGER);
			}
			ps.setLong(6, task.id());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to update task id=" + task.id(), e);
		}
	}

	@Override
	/**
	 * Deletes a single task by id.
	 *
	 * @param id task id
	 * @throws TasksDAOException when the delete fails
	 */
	public void deleteTask(int id) throws TasksDAOException {
		// Validate parameters
		if (id <= 0) {
			throw new TasksDAOException("id must be positive");
		}
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM TASKS WHERE ID = ?")) {
			ps.setLong(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to delete task id=" + id, e);
		}
	}

	@Override
	/**
	 * Deletes all tasks from the TASKS table.
	 *
	 * @throws TasksDAOException when the delete fails
	 */
	public void deleteTasks() throws TasksDAOException {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DELETE FROM TASKS");
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to delete all tasks", e);
		}
	}
}


