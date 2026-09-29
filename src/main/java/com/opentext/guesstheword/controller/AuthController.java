package com.opentext.guesstheword.controller;

import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.service.AuthService;
import com.opentext.guesstheword.service.GameException;
import com.opentext.guesstheword.service.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    // Post/Redirect/Get throughout: refreshing the result page can never resubmit the form.
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password,
                            RedirectAttributes redirect) {
        String error = ValidationUtil.validateUsername(username);
        if (error == null) {
            error = ValidationUtil.validatePassword(password);
        }
        if (error == null) {
            try {
                authService.register(username, password);
                redirect.addFlashAttribute("okMessage", "Registered. Please log in.");
                return "redirect:/login";
            } catch (GameException e) {
                error = e.getMessage();
            }
        }
        redirect.addFlashAttribute("errorMessage", error);
        return "redirect:/register";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                         HttpServletRequest request, RedirectAttributes redirect) {
        Optional<AppUser> user = authService.authenticate(username, password);
        if (user.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Invalid username or password.");
            return "redirect:/login";
        }
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate(); // clear out any previous session before starting a fresh one
        }
        HttpSession session = request.getSession(true);
        AppUser u = user.get();
        session.setAttribute("userId", u.getId());
        session.setAttribute("username", u.getUsername());
        session.setAttribute("role", u.getRole().label());
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
