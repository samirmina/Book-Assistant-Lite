package com.bookassistant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String message,
                                Model model) {
        log.info("=== Contact Form Submission ===");
        log.info("Name: {}", name);
        log.info("Email: {}", email);
        log.info("Message: {}", message);
        log.info("================================");

        model.addAttribute("success", "تم إرسال رسالتك بنجاح! شكراً لتواصلك معنا");
        return "contact";
    }
}
