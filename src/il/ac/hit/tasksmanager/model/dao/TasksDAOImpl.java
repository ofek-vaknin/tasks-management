package il.ac.hit.tasksmanager.model.dao;

import il.ac.hit.tasksmanager.model.Task;
import il.ac.hit.tasksmanager.model.BasicTask;
import il.ac.hit.tasksmanager.model.RecurringTask;
import il.ac.hit.tasksmanager.model.entities.TaskState;
import il.ac.hit.tasksmanager.model.entities.ToDoState;
import il.ac.hit.tasksmanager.model.entities.InProgressState;
import il.ac.hit.tasksmanager.model.entities.CompletedState;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * TasksDAOImpl is a Derby-backed DAO (Singleton) that persists tasks.
 */
public class TasksDAOImpl implements ITasksDAO {
	private static final String DB_URL = "jdbc:derby:tasksdb;create=true";
	private static TasksDAOImpl instance;

	private TasksDAOImpl() throws TasksDAOException {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			initSchema();
		} catch (ClassNotFoundException e) {
			throw new TasksDAOException("Derby EmbeddedDriver not found in classpath", e);
		}
	}

	public static synchronized TasksDAOImpl getInstance() throws TasksDAOException {
		if (instance == null) {
			instance = new TasksDAOImpl();
		}
		return instance;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL);
	}

	private void initSchema() throws TasksDAOException {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			try {
				stmt.executeUpdate(
					"CREATE TABLE TASKS (" +
					"ID BIGINT PRIMARY KEY, " +
					"TITLE VARCHAR(255) NOT NULL, " +
					"DESCRIPTION VARCHAR(1024), " +
					"STATE VARCHAR(32) NOT NULL, " +
					"DUEDATE DATE)"
				);
			} catch (SQLException e) {
				if (!"X0Y32".equals(e.getSQLState())) {
					throw e;
				}
			}
			try {
				stmt.executeUpdate("ALTER TABLE TASKS ADD COLUMN DUEDATE DATE");
			} catch (SQLException ignore) { }
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to initialize schema", e);
		}
	}

	private long nextId() throws TasksDAOException {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(ID) FROM TASKS")) {
			if (rs.next()) {
				long max = rs.getLong(1);
				if (rs.wasNull()) {
					return 1L;
				}
				return max + 1L;
			}
			return 1L;
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to generate next ID", e);
		}
	}

	@Override
	public Task addTask(Task task) throws TasksDAOException {
		Task toInsert = task;
		long id = nextId();
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO TASKS (ID, TITLE, DESCRIPTION, STATE, DUEDATE) VALUES (?, ?, ?, ?, ?)")) {
			ps.setLong(1, id);
			ps.setString(2, toInsert.title());
			ps.setString(3, toInsert.description());
			ps.setString(4, toInsert.state().name());
			if (toInsert.dueDate() != null) {
				ps.setDate(5, Date.valueOf(toInsert.dueDate()));
			} else {
				ps.setNull(5, Types.DATE);
			}
			ps.executeUpdate();
			if (toInsert instanceof il.ac.hit.tasksmanager.model.BasicTask b) {
				return new il.ac.hit.tasksmanager.model.BasicTask(id, b.title(), b.description(), b.state(), b.dueDate());
			} else if (toInsert instanceof il.ac.hit.tasksmanager.model.RecurringTask r) {
				return new il.ac.hit.tasksmanager.model.RecurringTask(id, r.title(), r.description(), r.state(), r.dueDate(), r.recurrenceInterval());
			}
			return toInsert;
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to add task", e);
		}
	}

	@Override
	public Task[] getTasks() throws TasksDAOException {
		List<Task> tasks = new ArrayList<>();
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ID, TITLE, DESCRIPTION, STATE, DUEDATE FROM TASKS ORDER BY ID")) {
			while (rs.next()) {
				long id = rs.getLong("ID");
				String title = rs.getString("TITLE");
				String description = rs.getString("DESCRIPTION");
				String stateStr = rs.getString("STATE");
				Date dueSql = rs.getDate("DUEDATE");
				TaskState state = switch (stateStr) {
					case "TODO" -> new ToDoState();
					case "IN_PROGRESS" -> new InProgressState();
					case "COMPLETED" -> new CompletedState();
					default -> new ToDoState();
				};
				tasks.add(new BasicTask(id, title, description, state, dueSql == null ? null : dueSql.toLocalDate()));
			}
			return tasks.toArray(new Task[0]);
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to fetch tasks", e);
		}
	}

	@Override
	public Task getTask(long id) throws TasksDAOException {
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT ID, TITLE, DESCRIPTION, STATE, DUEDATE FROM TASKS WHERE ID = ?")) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String title = rs.getString("TITLE");
					String description = rs.getString("DESCRIPTION");
					String stateStr = rs.getString("STATE");
					Date dueSql = rs.getDate("DUEDATE");
					TaskState state = switch (stateStr) {
						case "TODO" -> new ToDoState();
						case "IN_PROGRESS" -> new InProgressState();
						case "COMPLETED" -> new CompletedState();
						default -> new ToDoState();
					};
					long rid = rs.getLong("ID");
					return new BasicTask(rid, title, description, state, dueSql == null ? null : dueSql.toLocalDate());
				}
				return null;
			}
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to get task id=" + id, e);
		}
	}

	@Override
	public void updateTask(Task task) throws TasksDAOException {
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE TASKS SET TITLE = ?, DESCRIPTION = ?, STATE = ?, DUEDATE = ? WHERE ID = ?")) {
			ps.setString(1, task.title());
			ps.setString(2, task.description());
			ps.setString(3, task.state().name());
			if (task.dueDate() != null) {
				ps.setDate(4, Date.valueOf(task.dueDate()));
			} else {
				ps.setNull(4, Types.DATE);
			}
			ps.setLong(5, task.id());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to update task id=" + task.id(), e);
		}
	}

	@Override
	public void deleteTask(long id) throws TasksDAOException {
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM TASKS WHERE ID = ?")) {
			ps.setLong(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to delete task id=" + id, e);
		}
	}

	@Override
	public void deleteTasks() throws TasksDAOException {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DELETE FROM TASKS");
		} catch (SQLException e) {
			throw new TasksDAOException("Failed to delete all tasks", e);
		}
	}
}


