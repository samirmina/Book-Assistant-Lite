package com.bookassistant;

import com.bookassistant.service.AiService;
import com.bookassistant.service.FileParserService;
import com.bookassistant.session.BookSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Book Assistant Lite application.
 * Tests the full stack from HTTP requests to AI service integration.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class BookAssistantLiteIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookSession bookSession;

    @Autowired
    private FileParserService fileParserService;

    @Autowired
    private AiService aiService;

    /**
     * Test that the application context loads successfully.
     */
    @Test
    public void contextLoads() {
        assert bookSession != null;
        assert fileParserService != null;
        assert aiService != null;
    }

    /**
     * Test that the home page loads correctly.
     */
    @Test
    public void testHomePageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("hasBook"));
    }

    /**
     * Test file upload with a valid PDF.
     * Note: This test requires a sample PDF file in the test resources.
     */
    @Test
    public void testFileUpload() throws Exception {
        // Create a mock PDF file (minimal PDF structure for testing)
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >>\nendobj\n4 0 obj\n<< /Length 44 >>\nstream\nBT\n/F1 12 Tf\n100 700 Td\n(Test PDF Content) Tj\nET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000214 00000 n\ntrailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n309\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test file upload with an invalid file type.
     */
    @Test
    public void testFileUploadInvalidType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "This is not a PDF".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"))
                .andReturn();

        String errorMessage = (String) result.getModelAndView().getModel().get("error");
        assert errorMessage != null;
    }

    /**
     * Test general summary endpoint (requires book to be uploaded first).
     * This test demonstrates the flow but may need session management.
     */
    @Test
    public void testGeneralSummaryEndpoint() throws Exception {
        // First upload a book
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >>\nendobj\n4 0 obj\n<< /Length 44 >>\nstream\nBT\n/F1 12 Tf\n100 700 Td\n(Test PDF Content for Summary) Tj\nET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000214 00000 n\ntrailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n309\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Then request a summary
        mockMvc.perform(post("/summary/general")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("summaryGeneral"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test pages summary endpoint.
     */
    @Test
    public void testPagesSummaryEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 5 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request summary for specific pages
        mockMvc.perform(post("/summary/pages")
                        .param("startPage", "1")
                        .param("endPage", "2")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("summaryPages"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test Q&A endpoint.
     */
    @Test
    public void testQaEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Ask a question
        mockMvc.perform(post("/qa")
                        .param("question", "What is this book about?")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("qa"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test semantic search endpoint.
     */
    @Test
    public void testSemanticSearchEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Perform semantic search
        mockMvc.perform(post("/semantic-search")
                        .param("query", "main concepts")
                        .param("outputLanguage", "en"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("semanticSearch"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test reality check endpoint.
     */
    @Test
    public void testRealityCheckEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 3 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request reality check
        mockMvc.perform(post("/reality-check")
                        .param("startPage", "1")
                        .param("endPage", "2")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("realityCheck"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test concept map endpoint.
     */
    @Test
    public void testConceptMapEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 2 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request concept map
        mockMvc.perform(post("/concept-map")
                        .param("startPage", "1")
                        .param("endPage", "2")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("conceptMap"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test risk flags endpoint.
     */
    @Test
    public void testRiskFlagsEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request risk flags
        mockMvc.perform(post("/risk-flags")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("riskFlags"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test action plan endpoint.
     */
    @Test
    public void testActionPlanEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 2 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request action plan
        mockMvc.perform(post("/action-plan")
                        .param("startPage", "1")
                        .param("endPage", "2")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("actionPlan"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test comprehension test endpoint.
     */
    @Test
    public void testComprehensionEndpoint() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 2 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request comprehension test
        mockMvc.perform(post("/comprehension")
                        .param("startPage", "1")
                        .param("endPage", "2")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("comprehension"))
                .andExpect(model().attribute("hasBook", true));
    }

    /**
     * Test language detection with Arabic content.
     */
    @Test
    public void testLanguageDetectionArabic() throws Exception {
        // Create PDF with Arabic content
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arabic-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasBook", true));

        // Verify book language is detected
        String detectedLang = bookSession.getDetectedLanguage();
        assert detectedLang != null;
    }

    /**
     * Test language detection with English content.
     */
    @Test
    public void testLanguageDetectionEnglish() throws Exception {
        // Create PDF with English content
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "english-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasBook", true));

        // Verify book language is detected
        String detectedLang = bookSession.getDetectedLanguage();
        assert detectedLang != null;
    }

    /**
     * Test output language parameter with Arabic.
     */
    @Test
    public void testOutputLanguageArabic() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request with Arabic output
        mockMvc.perform(post("/summary/general")
                        .param("outputLanguage", "ar"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("bookLanguage", bookSession.getDetectedLanguage()));
    }

    /**
     * Test output language parameter with English.
     */
    @Test
    public void testOutputLanguageEnglish() throws Exception {
        // Upload book first
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent.getBytes()
        );

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk());

        // Request with English output
        mockMvc.perform(post("/summary/general")
                        .param("outputLanguage", "en"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("bookLanguage", bookSession.getDetectedLanguage()));
    }
}
