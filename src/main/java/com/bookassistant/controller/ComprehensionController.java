package com.bookassistant.controller;

import com.bookassistant.service.AiService;
import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ComprehensionController {

    private final BookSession bookSession;
    private final AiService aiService;

    public ComprehensionController(BookSession bookSession, AiService aiService) {
        this.bookSession = bookSession;
        this.aiService = aiService;
    }

    @PostMapping("/comprehension")
    public String comprehension(@RequestParam int startPage,
                                @RequestParam int endPage,
                                @RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                                Model model) {
        String text = bookSession.extractRange(startPage, endPage);
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String answer = aiService.generateComprehensionTest(text, startPage, endPage, lang);
        model.addAttribute("comprehension", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }
}

