package com.bookassistant.controller;

import com.bookassistant.service.FileParserService;
import com.bookassistant.session.BookSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class UploadController {

    private final FileParserService fileParserService;
    private final BookSession bookSession;

    public UploadController(FileParserService fileParserService, BookSession bookSession) {
        this.fileParserService = fileParserService;
        this.bookSession = bookSession;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            bookSession.clear();
            bookSession.setBook(fileParserService.parse(file));
            model.addAttribute("hasBook", true);
            model.addAttribute("success", "تم رفع الكتاب بنجاح");
        } catch (Exception e) {
            model.addAttribute("error", "فشل في قراءة الملف: " + e.getMessage());
            model.addAttribute("hasBook", false);
        }
        return "index";
    }
}

