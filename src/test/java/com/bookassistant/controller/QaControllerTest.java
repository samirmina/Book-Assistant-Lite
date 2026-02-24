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
 * Unit tests for QaController.
 */
@ExtendWith(MockitoExtension.class)
class QaControllerTest {

    @Mock
    private BookSession bookSession;

    @Mock
    private AiService aiService;

    @Mock
    private Model model;

    @InjectMocks
    private QaController qaController;

    private BookData testBook;

    @BeforeEach
    void setUp() {
        List<PageData> pages = List.of(
            new PageData(1, "Page 1 content with answer"),
            new PageData(2, "Page 2 content")
        );
        testBook = new BookData("test.pdf", pages, "Full book content with the answer");
    }

    @Test
    void testQa_Success() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("إجابة السؤال");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = qaController.qa("ما هو الموضوع الرئيسي؟", null, model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(
            argThat(s -> s != null && s.contains("أجب عن السؤال")),
            argThat(s -> s != null && s.contains("ما هو الموضوع الرئيسي؟")),
            eq("ar")
        );
        verify(model).addAttribute("qa", "إجابة السؤال");
        verify(model).addAttribute("hasBook", true);
        verify(model).addAttribute("bookLanguage", "ar");
    }

    @Test
    void testQa_EnglishOutput() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("Answer to the question");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = qaController.qa("What is the main topic?", "en", model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(
            argThat(s -> s != null && s.contains("Answer")),
            argThat(s -> s != null && s.contains("What is the main topic?")),
            eq("en")
        );
    }

    @Test
    void testQa_NoBook() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.empty());
        when(bookSession.hasBook()).thenReturn(false);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = qaController.qa("Any question", null, model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(anyString(), argThat(s -> s != null && s.contains("Any question")), eq("ar"));
    }

    @Test
    void testQa_UserContentIsBilingual() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("en");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("Answer");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act - English question
        qaController.qa("What is this?", "en", model);

        // Assert - user content should be in English
        verify(aiService).ask(
            anyString(),
            argThat(content -> content.contains("Question:") && content.contains("What is this?")),
            eq("en")
        );
    }

    @Test
    void testQa_ArabicQuestion() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("الجواب");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act - Arabic question
        qaController.qa("ما هذا؟", "ar", model);

        // Assert - user content should be in Arabic
        verify(aiService).ask(
            anyString(),
            argThat(content -> content.contains("السؤال:") && content.contains("ما هذا؟")),
            eq("ar")
        );
    }

    @Test
    void testControllerCreation() {
        assertNotNull(qaController);
    }

    @Test
    void testQa_WithComplexQuestion() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("ar");
        String complexQuestion = "ما هي الفصول الرئيسية المذكورة في الكتاب؟ وما هي الأفكار الأساسية؟";
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("إجابة مفصلة");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        String view = qaController.qa(complexQuestion, "ar", model);

        // Assert
        assertEquals("index", view);
        verify(aiService).ask(
            anyString(),
            argThat(content -> content.contains(complexQuestion)),
            eq("ar")
        );
    }

    @Test
    void testQa_ModelAttributes() {
        // Arrange
        when(bookSession.getBook()).thenReturn(Optional.of(testBook));
        when(bookSession.hasBook()).thenReturn(true);
        when(bookSession.getDetectedLanguage()).thenReturn("en");
        when(aiService.ask(anyString(), anyString(), anyString())).thenReturn("Answer");
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act
        qaController.qa("Test question", "en", model);

        // Assert - verify all required model attributes are set
        verify(model).addAttribute("qa", "Answer");
        verify(model).addAttribute("hasBook", true);
        verify(model).addAttribute("bookLanguage", "en");
    }
}
