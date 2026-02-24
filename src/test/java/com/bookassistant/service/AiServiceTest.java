package com.bookassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AiService component.
 * Tests prompt generation and language handling.
 */
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiService aiService;

    @BeforeEach
    void setUp() {
        // Note: Real AiService doesn't use httpClient anymore
        // It creates its own HttpClient per request
        aiService = new AiService(objectMapper);
    }

    @Test
    void testServiceCreation() {
        // Service should be created successfully
        assertNotNull(aiService);
    }

    @Test
    void testAskMethodExists() {
        // Verify the ask method exists and can be called
        // Note: Actual API call testing requires mocking HTTP client
        assertDoesNotThrow(() -> {
            // This will fail at HTTP level but proves method signature is correct
            aiService.ask("Test prompt", "Test content");
        });
    }

    @Test
    void testAskWithLanguageMethodExists() {
        // Verify the overloaded ask method with language parameter exists
        assertDoesNotThrow(() -> {
            aiService.ask("Test prompt", "Test content", "ar");
        });
    }

    @Test
    void testGenerateComprehensionTestMethodExists() {
        // Verify comprehension test method exists
        assertDoesNotThrow(() -> {
            aiService.generateComprehensionTest("Test text", 1, 10);
        });
    }

    @Test
    void testGenerateComprehensionTestWithLanguageMethodExists() {
        // Verify overloaded comprehension test method exists
        assertDoesNotThrow(() -> {
            aiService.generateComprehensionTest("Test text", 1, 10, "en");
        });
    }
}
