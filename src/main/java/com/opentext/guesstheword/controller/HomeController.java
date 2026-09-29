package com.opentext.guesstheword.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // The "/" path is already guarded by AuthInterceptor(null): reaching here means a
    // session exists, so "role" is guaranteed to be set.
    @GetMapping("/")
    public String home(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "redirect:/" + ("admin".equals(role) ? "admin" : "play");
    }
}
