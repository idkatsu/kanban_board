package com.controllers;

import com.dao.UserDAO;
import com.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserDAO userDAO;

    @Autowired
    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // отображение страницы авторизации
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("title", "Авторизация");
        return "auth/login";
    }

    // обработка формы авторизации
    @PostMapping("/login")
    public String login(
            @ModelAttribute("user") User user,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User existingUser = userDAO.findByUsername(user.getUsername());

        if (existingUser != null && userDAO.isValidUser(user.getUsername(), user.getPassword())) {
            session.setAttribute("userId", existingUser.getId());
            return "redirect:/home";
        } else {
            redirectAttributes.addFlashAttribute("error", "Неверный логин или пароль");
            return "redirect:/auth/login";
        }
    }

    // отображение страницы регистрации
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    // обработка формы регистрации
    @PostMapping("/register")
    public String register(
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {

        if (userDAO.userExists(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Пользователь с таким логином уже существует");
            return "redirect:/auth/register";
        }

        userDAO.registerUser(user);
        redirectAttributes.addFlashAttribute("message", "Регистрация успешна! Войдите в систему.");
        return "redirect:/auth/login";
    }
}