package com.bookassistant.controller;

import com.bookassistant.service.AiService;
import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlanController {

    private final BookSession bookSession;
    private final AiService aiService;

    public PlanController(BookSession bookSession, AiService aiService) {
        this.bookSession = bookSession;
        this.aiService = aiService;
    }

    @PostMapping("/action-plan")
    public String actionPlan(@RequestParam int startPage,
                             @RequestParam int endPage,
                             @RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                             Model model) {
        String text = bookSession.extractRange(startPage, endPage);
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang) 
            ? "Convert these pages into an actionable action plan." 
            : "حوّل هذه الصفحات إلى خطة تنفيذية عملية.";
        String answer = aiService.ask(systemPrompt, text, lang);
        model.addAttribute("actionPlan", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }
}

