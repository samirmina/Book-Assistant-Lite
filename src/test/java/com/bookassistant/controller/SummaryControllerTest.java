package com.bookassistant.controller;

import com.bookassistant.model.BookData;
import com.bookassistant.model.PageData;
import com.bookassistant.service.AiService;
import com.bookassistant.session.BookSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SummaryController.
 */
@ExtendWith(MockitoExtension.class)
class SummaryControllerTest {

    @Mock
    private BookSession bookSession;

    @Mock
    private AiService aiService;

    @Mock
    private Model model;

    @InjectMocks
    private SummaryController summaryController;

    private BookData testBook;

    @BeforeEach
    void setUp() {
        List<PageData> pages = List.of(
            new PageData(1, "Page 1 content"),
            new PageData(2, "Page 2 content")
        );
        testBook = new BookData("test.pdf", pages, "Full book content");
    }

    @Test
    void testGeneralSummary_Success() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("ملخص الكتاب");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.generalSummary(null, model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(anyString(), eq("Full book content"), eq("ar"));
        verify(model).addAttribute("summaryGeneral", "ملخص الكتاب");
        verify(model).addAttribute("hasBook", true);
        verify(model).addAttribute("bookLanguage", "ar");
    }

    @Test
    void testGeneralSummary_EnglishOutput() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("en");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("Book summary");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.generalSummary("en", model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(anyString(), eq("Full book content"), eq("en"));
        verify(model).addAttribute("summaryGeneral", "Book summary");
    }

    @Test
    void testGeneralSummary_NoBook() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.empty());
        when(bookSession.hasBook()).thenReturn(false);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.generalSummary(null, model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(anyString(), eq(""), eq("ar"));
    }

    @Test
    void testPagesSummary_Success() {
        // Arrange
        when(bookSession.extractRange(1, 2)).thenReturn("Page 1 content\n\nPage 2 content");
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("ملخص الصفحات");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.pagesSummary(1, 2, null, model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(anyString(), argThat(s -> s != null && s.contains("Page 1 content")), eq("ar"));
        verify(model).addAttribute("summaryPages", "ملخص الصفحات");
        verify(model).addAttribute("hasBook", true);
    }

    @Test
    void testPagesSummary_EnglishOutput() {
        // Arrange
        when(bookSession.extractRange(1, 5)).thenReturn("Content from pages 1-5");
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("en");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("Pages summary");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.pagesSummary(1, 5, "en", model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(anyString(), eq("Content from pages 1-5"), eq("en"));
    }

    @Test
    void testPagesSummary_InvalidRange() {
        // Arrange
        when(bookSession.extractRange(10, 5)).thenReturn("");
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.pagesSummary(10, 5, null, model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(anyString(), eq(""), eq("ar"));
    }

    @Test
    void testControllerCreation() {
        // Verify controller can be created
        assertNotNull(summaryController);
    }

    @Test
    void testLanguageFallbackToBookLanguage() {
        // Arrange - outputLanguage is null, should use book language
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("en");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("Summary");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.generalSummary(null, model);

        // Assert - should use book language (en) when outputLanguage is null
        verify(aiService).ask(anyString(), anyString(), eq("en"));
    }

    @Test
    void testOutputLanguageOverridesBookLanguage() {
        // Arrange - outputLanguage is "ar" but book is English
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("en");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("ملخص");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = summaryController.generalSummary("ar", model);

        // Assert - should use outputLanguage (ar) even though book is English
        verify(aiService).ask(anyString(), anyString(), eq("ar"));
    }
}
