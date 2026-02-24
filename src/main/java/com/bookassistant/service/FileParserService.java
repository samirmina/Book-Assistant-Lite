package com.bookassistant.service;

import com.bookassistant.model.BookData;
import com.bookassistant.model.PageData;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileParserService {

    public BookData parse(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = doc.getNumberOfPages();
            List<PageData> pages = new ArrayList<>(pageCount);
            StringBuilder full = new StringBuilder();

            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(doc).trim();
                pages.add(new PageData(i, pageText));
                full.append(pageText).append("\n\n");
            }
            return new BookData(file.getOriginalFilename(), pages, full.toString());
        }
    }
}

