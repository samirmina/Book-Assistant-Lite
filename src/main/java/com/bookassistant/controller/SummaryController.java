package com.bookassistant.controller;

import com.bookassistant.service.AiService;
import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SummaryController {

    private final BookSession bookSession;
    private final AiService aiService;

    public SummaryController(BookSession bookSession, AiService aiService) {
        this.bookSession = bookSession;
        this.aiService = aiService;
    }

    @PostMapping("/summary/general")
    public String generalSummary(@RequestParam(value = "outputLanguage", required = false) String outputLanguage, Model model) {
        String text = bookSession.getBook().map(b -> b.fullText()).orElse("");
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang) 
            ? "Summarize the book concisely." 
            : "لخّص الكتاب بإيجاز.";
        String answer = aiService.ask(systemPrompt, text, lang);
        model.addAttribute("summaryGeneral", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }

    @PostMapping("/summary/pages")
    public String pagesSummary(@RequestParam int startPage,
                               @RequestParam int endPage,
                               @RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                               Model model) {
        String text = bookSession.extractRange(startPage, endPage);
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang) 
            ? "Summarize these pages concisely." 
            : "لخّص هذه الصفحات بإيجاز.";
        String answer = aiService.ask(systemPrompt, text, lang);
        model.addAttribute("summaryPages", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }
}

