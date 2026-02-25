package com.bookassistant.controller;

import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private final BookSession bookSession;

    public PageController(BookSession bookSession) {
        this.bookSession = bookSession;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/app")
    public String app(Model model) {
        model.addAttribute("hasBook", bookSession.getBook().isPresent());
        model.addAttribute("bookLanguage", bookSession.getDetectedLanguage());
        return "index";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email, Model model) {
        // In a real app, this would send a reset email
        // For now, just show success message
        model.addAttribute("success", true);
        return "forgot-password";
    }
}
