package com.smartFood.donation.controller;

import com.smartFood.donation.models.User;
import com.smartFood.donation.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (user.getName() == null || user.getName().trim().isEmpty() ||
            user.getEmail() == null || user.getEmail().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Please fill in all required fields.");
            return "register";
        }

        boolean isRegistered = userService.registerUser(user);
        if (!isRegistered) {
            model.addAttribute("error", "An account with this email already exists! Please login instead.");
            return "register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please sign in.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        Optional<User> user = userService.authenticate(email, password);

        if (user.isPresent()) {
            User loggedIn = user.get();
            session.setAttribute("loggedInUser", loggedIn);
            session.setAttribute("userRole", loggedIn.getRole());
            redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + loggedIn.getName() + "!");
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password. Please check your credentials.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/login";
    }
}