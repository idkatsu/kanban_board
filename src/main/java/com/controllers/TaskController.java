package com.controllers;

import com.dao.TaskDAO;
import com.dao.UserDAO;
import com.models.Task;
import com.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;


@Controller
@RequestMapping("/home")
public class TaskController {

    private final TaskDAO taskDAO;
    private final UserDAO userDAO;

    @Autowired
    public TaskController(TaskDAO taskDAO, UserDAO userDAO) {
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
    }

    // Отображение главной страницы
    @GetMapping
    public String homePage(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("tasks", taskDAO.findByUserId(userId));
        } else {
            model.addAttribute("error", "Необходимо авторизоваться");
            return "auth/login";
        }

        model.addAttribute("users", userDAO.findAll());
        return "tasks/home";
    }

    // Создание задачи
    @PostMapping("/create-card")
    public String createTask(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("assigneeId") Long assigneeId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = (Long) session.getAttribute("userId");

        if (currentUserId == null) {
            redirectAttributes.addFlashAttribute("error", "Необходимо авторизоваться");
            return "redirect:/auth/login";
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("OPEN");
        task.setCreatedBy(currentUserId);

        User assignee = userDAO.findById(assigneeId);
        if (assignee != null) {
            task.setAssignee(assignee);
        } else {
            redirectAttributes.addFlashAttribute("error", "Исполнитель не найден.");
            return "redirect:/home";
        }

        taskDAO.save(task);
        redirectAttributes.addFlashAttribute("message", "Задача успешно создана!");

        return "redirect:/home";
    }

    // Изменение статуса задачи
    @PostMapping("/update-status")
    public String updateStatus(
            @RequestParam("taskId") Long taskId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        taskDAO.updateStatus(taskId, status);
        redirectAttributes.addFlashAttribute("message", "Статус задачи обновлен!");
        return "redirect:/home";
    }

    // Назначение исполнителя задачи
    @PostMapping("/assign-task")
    public String assignTask(
            @RequestParam("taskId") Long taskId,
            @RequestParam("assigneeId") Long assigneeId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = (Long) session.getAttribute("userId");

        Task task = taskDAO.findById(taskId);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена.");
            return "redirect:/home";
        }

        if (task.getAssignee() != null && task.getAssignee().getId().equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на изменение этой задачи.");
            return "redirect:/home";
        }

        User assignee = userDAO.findById(assigneeId);
        if (assignee != null) {
            taskDAO.assignTask(taskId, assigneeId);
            redirectAttributes.addFlashAttribute("message", "Исполнитель задачи обновлен!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Исполнитель не найден.");
        }

        return "redirect:/home";
    }

    // Обработка выхода из системы
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // Удаление задачи
    @PostMapping("/delete-task")
    public String deleteTask(@RequestParam("taskId") Long taskId, RedirectAttributes redirectAttributes) {
        taskDAO.delete(taskId);
        redirectAttributes.addFlashAttribute("message", "Задача успешно удалена!");
        return "redirect:/home";
    }
}
