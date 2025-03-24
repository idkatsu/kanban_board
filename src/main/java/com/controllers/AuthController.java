package com.controllers;

import com.dao.UserDAO;
import com.models.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("title", "Авторизация");
        return "auth/login";
    }

    // обработка формы авторизации
    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        User user = userDAO.findByUsername(username);

        if (user != null && userDAO.isValidUser(username, password)) {
            session.setAttribute("userId", user.getId());
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Неверный логин или пароль");
            return "auth/login";
        }
    }

    // отображение страницы регистрации
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("title", "Регистрация");
        return "auth/register";
    }

    // обработка формы регистрации
    @PostMapping("/register")
    public String register(
            @ModelAttribute("user") @Valid User user,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (userDAO.userExists(user.getUsername())) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            return "auth/register";
        }

        userDAO.registerUser(user);
        return "redirect:/auth/login";
    }
}