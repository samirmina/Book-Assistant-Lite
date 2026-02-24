package com.bookassistant.controller;

import com.bookassistant.service.AiService;
import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QaController {

    private final BookSession bookSession;
    private final AiService aiService;

    public QaController(BookSession bookSession, AiService aiService) {
        this.bookSession = bookSession;
        this.aiService = aiService;
    }

    @PostMapping("/qa")
    public String qa(@RequestParam String question,
                     @RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                     Model model) {
        String text = bookSession.getBook().map(b -> b.fullText()).orElse("");
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang)
            ? "Answer the question based only on the book content."
            : "أجب عن السؤال بناءً على محتوى الكتاب فقط.";
        String userContent = "en".equals(lang)
            ? "Question: " + question + "\n\nText:\n" + text
            : "السؤال: " + question + "\n\nالنص:\n" + text;
        String answer = aiService.ask(systemPrompt, userContent, lang);
        model.addAttribute("qa", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }
}

