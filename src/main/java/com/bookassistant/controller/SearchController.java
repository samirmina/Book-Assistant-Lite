package com.bookassistant.controller;

import com.bookassistant.service.AiService;
import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final BookSession bookSession;
    private final AiService aiService;

    public SearchController(BookSession bookSession, AiService aiService) {
        this.bookSession = bookSession;
        this.aiService = aiService;
    }

    @PostMapping("/semantic-search")
    public String semanticSearch(@RequestParam String query,
                                 @RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                                 Model model) {
        String text = bookSession.getBook().map(b -> b.fullText()).orElse("");
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang)
            ? "Search semantically in the text and answer the question."
            : "ابحث دلالياً في النص وأجب عن السؤال.";
        String userContent = "en".equals(lang)
            ? "Query: " + query + "\n\nText:\n" + text
            : "السؤال: " + query + "\n\nالنص:\n" + text;
        String answer = aiService.ask(systemPrompt, userContent, lang);
        model.addAttribute("semanticSearch", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }
}

