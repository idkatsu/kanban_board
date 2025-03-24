package com.dao;

import com.models.Task;
import com.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskDAO {

    private final JdbcTemplate jdbcTemplate;
    private final UserDAO userDAO;

    @Autowired
    public TaskDAO(JdbcTemplate jdbcTemplate, UserDAO userDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDAO = userDAO;
    }

        // поиск задачи по id
        public Task findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setStatus(rs.getString("status"));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Long assignedTo = rs.getLong("assigned_to");
            if (assignedTo != null) {
                User assignee = userDAO.findById(assignedTo);
                task.setAssignee(assignee);
            }

            return task;
        });
    }
    // сохранение новой задачи
    public void save(Task task) {
        String sql = "INSERT INTO tasks (title, description, created_by, assigned_to, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, task.getTitle(), task.getDescription(),
                task.getCreatedBy(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                "OPEN", java.sql.Timestamp.valueOf(task.getCreatedAt()));
    }
    // поменять статус
    public boolean updateStatus(Long taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, status, taskId);
        return rowsUpdated > 0;
    }
    // поменять исполнителя
    public boolean assignTask(Long taskId, Long assigneeId) {
        String sql = "UPDATE tasks SET assigned_to = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, assigneeId, taskId);
        return rowsUpdated > 0;
    }
    // удалить задачу
    public void delete(Long taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        jdbcTemplate.update(sql, taskId);
    }
    // поиск всех задач по id пользователя
    public List<Task> findByUserId(Long userId) {
        String sql = "SELECT * FROM tasks WHERE created_by = ? OR assigned_to = ?";
        return jdbcTemplate.query(sql, new Object[]{userId, userId}, (rs, rowNum) -> {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setStatus(rs.getString("status"));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Long assignedTo = rs.getLong("assigned_to");
            if (assignedTo != null) {
                User assignee = userDAO.findById(assignedTo);
                task.setAssignee(assignee);
            }

            return task;
        });
    }
}
