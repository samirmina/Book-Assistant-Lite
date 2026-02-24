package com.bookassistant.service;

import com.bookassistant.model.BookData;
import com.bookassistant.model.PageData;
import com.bookassistant.session.BookSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookSession component.
 * Tests language detection and book management functionality.
 */
class BookSessionTest {

    private BookSession bookSession;

    @BeforeEach
    void setUp() {
        bookSession = new BookSession();
    }

    @Test
    void testInitialSessionState() {
        // Session should start empty
        assertFalse(bookSession.hasBook());
        assertTrue(bookSession.getBook().isEmpty());
        assertEquals("ar", bookSession.getDetectedLanguage());
    }

    @Test
    void testSetBookAndGetBook() {
        // Create a test book
        BookData book = new BookData("test.pdf", List.of(), "Test content");
        
        // Set the book
        bookSession.setBook(book);
        
        // Verify book is stored
        assertTrue(bookSession.hasBook());
        assertTrue(bookSession.getBook().isPresent());
        assertEquals("test.pdf", bookSession.getBook().get().fileName());
    }

    @Test
    void testClearSession() {
        // Set a book first
        BookData book = new BookData("test.pdf", List.of(), "Test content");
        bookSession.setBook(book);
        
        // Clear the session
        bookSession.clear();
        
        // Verify session is cleared
        assertFalse(bookSession.hasBook());
        assertTrue(bookSession.getBook().isEmpty());
    }

    @Test
    void testLanguageDetection_ArabicContent() {
        // Create book with predominantly Arabic content
        String arabicText = "هذا نص عربي يحتوي على كلمات عربية كثيرة جداً";
        BookData book = new BookData("arabic.pdf", List.of(), arabicText);
        
        bookSession.setBook(book);
        
        assertEquals("ar", bookSession.getDetectedLanguage());
    }

    @Test
    void testLanguageDetection_EnglishContent() {
        // Create book with predominantly English content
        String englishText = "This is an English text with many English words and sentences.";
        BookData book = new BookData("english.pdf", List.of(), englishText);
        
        bookSession.setBook(book);
        
        assertEquals("en", bookSession.getDetectedLanguage());
    }

    @Test
    void testLanguageDetection_MixedContentMoreArabic() {
        // Create book with mixed content but more Arabic characters
        // Arabic text has more characters than English
        String mixedText = "English. هذا نص عربي طويل جداً يحتوي على كلمات عربية كثيرة أكثر من الإنجليزية. More Arabic نص عربي طويل وأكثر";
        BookData book = new BookData("mixed.pdf", List.of(), mixedText);
        
        bookSession.setBook(book);
        
        // Should detect Arabic since it has more Arabic characters
        assertEquals("ar", bookSession.getDetectedLanguage());
    }

    @Test
    void testLanguageDetection_EmptyContent() {
        // Empty content should default to Arabic
        BookData book = new BookData("empty.pdf", List.of(), "");
        
        bookSession.setBook(book);
        
        assertEquals("ar", bookSession.getDetectedLanguage());
    }

    @Test
    void testExtractRange() {
        // Create book with multiple pages
        List<PageData> pages = List.of(
            new PageData(1, "Page 1 content"),
            new PageData(2, "Page 2 content"),
            new PageData(3, "Page 3 content"),
            new PageData(4, "Page 4 content"),
            new PageData(5, "Page 5 content")
        );
        
        BookData book = new BookData("multipage.pdf", pages, "Full text");
        bookSession.setBook(book);
        
        // Extract pages 2-4
        String extracted = bookSession.extractRange(2, 4);
        
        assertTrue(extracted.contains("Page 2 content"));
        assertTrue(extracted.contains("Page 3 content"));
        assertTrue(extracted.contains("Page 4 content"));
        assertFalse(extracted.contains("Page 1 content"));
        assertFalse(extracted.contains("Page 5 content"));
    }

    @Test
    void testExtractRange_NoBook() {
        // Should return empty string when no book is loaded
        String extracted = bookSession.extractRange(1, 10);
        assertTrue(extracted.isEmpty());
    }

    @Test
    void testExtractRange_InvalidRange() {
        // Create book with pages
        List<PageData> pages = List.of(
            new PageData(1, "Page 1"),
            new PageData(2, "Page 2")
        );
        BookData book = new BookData("test.pdf", pages, "Full text");
        bookSession.setBook(book);
        
        // Invalid range should return empty
        String extracted = bookSession.extractRange(10, 20);
        assertTrue(extracted.isEmpty());
    }

    @Test
    void testExtractRange_SinglePage() {
        // Create book with pages
        List<PageData> pages = List.of(
            new PageData(1, "Page 1"),
            new PageData(2, "Page 2"),
            new PageData(3, "Page 3")
        );
        BookData book = new BookData("test.pdf", pages, "Full text");
        bookSession.setBook(book);
        
        // Extract single page
        String extracted = bookSession.extractRange(2, 2);
        assertEquals("Page 2", extracted);
    }

    @Test
    void testLanguageDetection_LargeText() {
        // Test with large text (should only sample first 1000 chars)
        StringBuilder largeArabicText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeArabicText.append("نص عربي ");
        }
        
        BookData book = new BookData("large.pdf", List.of(), largeArabicText.toString());
        bookSession.setBook(book);
        
        assertEquals("ar", bookSession.getDetectedLanguage());
    }
}
