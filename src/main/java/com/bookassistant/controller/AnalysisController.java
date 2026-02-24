package com.bookassistant.controller;

import com.bookassistant.service.AiService;
import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnalysisController {

    private final BookSession bookSession;
    private final AiService aiService;

    public AnalysisController(BookSession bookSession, AiService aiService) {
        this.bookSession = bookSession;
        this.aiService = aiService;
    }

    @PostMapping("/reality-check")
    public String realityCheck(@RequestParam int startPage,
                               @RequestParam int endPage,
                               @RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                               Model model) {
        String text = bookSession.extractRange(startPage, endPage);
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang) 
            ? "Check the realism and assumptions in this text." 
            : "افحص مدى واقعية وافتراضات هذا النص.";
        String answer = aiService.ask(systemPrompt, text, lang);
        model.addAttribute("realityCheck", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }

    @PostMapping("/concept-map")
    public String conceptMap(@RequestParam int startPage,
                             @RequestParam int endPage,
                             @RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                             Model model) {
        String text = bookSession.extractRange(startPage, endPage);
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang) 
            ? "Create a text-based concept map of the most important concepts and relationships." 
            : "أنشئ خريطة مفاهيم نصية لأهم المفاهيم والعلاقات.";
        String answer = aiService.ask(systemPrompt, text, lang);
        model.addAttribute("conceptMap", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }

    @PostMapping("/risk-flags")
    public String riskFlags(@RequestParam(value = "outputLanguage", required = false) String outputLanguage,
                            Model model) {
        String text = bookSession.getBook().map(b -> b.fullText()).orElse("");
        String bookLang = bookSession.getDetectedLanguage();
        String lang = outputLanguage != null ? outputLanguage : bookLang;
        String systemPrompt = "en".equals(lang) 
            ? "Extract risk alerts or important warnings from this book." 
            : "استخرج تنبيهات مخاطر أو تحذيرات مهمة من هذا الكتاب.";
        String answer = aiService.ask(systemPrompt, text, lang);
        model.addAttribute("riskFlags", answer);
        model.addAttribute("hasBook", bookSession.hasBook());
        model.addAttribute("bookLanguage", bookLang);
        return "index";
    }
}

