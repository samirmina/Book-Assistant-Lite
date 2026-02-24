package com.bookassistant.session;

import com.bookassistant.model.BookData;
import com.bookassistant.model.PageData;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@SessionScope
public class BookSession {

    private static final Pattern ARABIC_PATTERN = Pattern.compile("[\\u0600-\\u06FF]");
    
    private BookData currentBook;
    private String detectedLanguage;

    public void setBook(BookData book) {
        this.currentBook = book;
        this.detectedLanguage = detectLanguage(book.fullText());
    }

    public Optional<BookData> getBook() {
        return Optional.ofNullable(currentBook);
    }

    public boolean hasBook() {
        return currentBook != null;
    }

    public String getDetectedLanguage() {
        return detectedLanguage != null ? detectedLanguage : "ar";
    }

    private String detectLanguage(String text) {
        if (text == null || text.isEmpty()) return "ar";
        // Check first 1000 characters for language detection
        String sample = text.length() > 1000 ? text.substring(0, 1000) : text;
        int arabicChars = 0;
        int latinChars = 0;
        
        for (char c : sample.toCharArray()) {
            if (ARABIC_PATTERN.matcher(String.valueOf(c)).find()) {
                arabicChars++;
            } else if (Character.isLetter(c) && c < 128) {
                latinChars++;
            }
        }
        
        // If Arabic characters are more or equal, consider it Arabic
        return arabicChars >= latinChars ? "ar" : "en";
    }

    public String extractRange(int startPage, int endPage) {
        if (currentBook == null) return "";
        return currentBook.pages().stream()
                .filter(p -> p.pageNumber() >= startPage && p.pageNumber() <= endPage)
                .map(PageData::text)
                .collect(Collectors.joining("\n\n"));
    }

    public void clear() {
        this.currentBook = null;
        this.detectedLanguage = null;
    }
}

