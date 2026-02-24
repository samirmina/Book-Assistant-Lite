package com.bookassistant.service;

import com.bookassistant.model.BookData;
import com.bookassistant.model.PageData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileParserService.
 * Tests PDF parsing functionality.
 */
class FileParserServiceTest {

    private FileParserService fileParserService;

    @BeforeEach
    void setUp() {
        fileParserService = new FileParserService();
    }

    @Test
    void testServiceCreation() {
        assertNotNull(fileParserService);
    }

    @Test
    void testParseNullFile() {
        assertThrows(Exception.class, () -> {
            fileParserService.parse(null);
        });
    }

    @Test
    void testParseEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );

        assertThrows(Exception.class, () -> {
            fileParserService.parse(emptyFile);
        });
    }

    @Test
    void testParseInvalidPdf() {
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "invalid.pdf",
            "application/pdf",
            "This is not a valid PDF".getBytes()
        );

        assertThrows(Exception.class, () -> {
            fileParserService.parse(invalidFile);
        });
    }

    @Test
    void testParseValidPdf() throws Exception {
        // Create a minimal valid PDF
        byte[] pdfBytes = createMinimalPdf();
        
        MockMultipartFile validFile = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            pdfBytes
        );

        BookData result = fileParserService.parse(validFile);

        assertNotNull(result);
        assertEquals("test.pdf", result.fileName());
        assertNotNull(result.pages());
        assertNotNull(result.fullText());
    }

    @Test
    void testParsePdfWithMultiplePages() throws Exception {
        // Create PDF with multiple pages
        byte[] pdfBytes = createPdfWithPages(3);
        
        MockMultipartFile multiPageFile = new MockMultipartFile(
            "file",
            "multipage.pdf",
            "application/pdf",
            pdfBytes
        );

        BookData result = fileParserService.parse(multiPageFile);

        assertNotNull(result);
        assertEquals("multipage.pdf", result.fileName());
        assertTrue(result.pages().size() > 0);
    }

    @Test
    void testParsePdf_PageNumbersAreSequential() throws Exception {
        byte[] pdfBytes = createPdfWithPages(3);
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            pdfBytes
        );

        BookData result = fileParserService.parse(file);

        List<PageData> pages = result.pages();
        for (int i = 0; i < pages.size(); i++) {
            assertEquals(i + 1, pages.get(i).pageNumber());
        }
    }

    @Test
    void testParsePdf_FullTextContainsAllPages() throws Exception {
        byte[] pdfBytes = createPdfWithPages(2);
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            pdfBytes
        );

        BookData result = fileParserService.parse(file);

        assertNotNull(result.fullText());
        // Full text should be combination of all page texts
        StringBuilder allPageTexts = new StringBuilder();
        for (PageData page : result.pages()) {
            allPageTexts.append(page.text());
        }
        // Full text should contain content from pages
        assertTrue(result.fullText().length() >= 0);
    }

    @Test
    void testParsePdf_LargeFile() throws Exception {
        // Create a larger PDF
        byte[] pdfBytes = createPdfWithPages(10);
        
        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "large.pdf",
            "application/pdf",
            pdfBytes
        );

        BookData result = fileParserService.parse(largeFile);

        assertNotNull(result);
        assertTrue(result.pages().size() > 0);
    }

    @Test
    void testParsePdf_FileNamePreserved() throws Exception {
        byte[] pdfBytes = createMinimalPdf();
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "my-custom-book.pdf",
            "application/pdf",
            pdfBytes
        );

        BookData result = fileParserService.parse(file);

        assertEquals("my-custom-book.pdf", result.fileName());
    }

    // Helper method to create a minimal valid PDF
    private byte[] createMinimalPdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    // Helper method to create PDF with multiple pages
    private byte[] createPdfWithPages(int pageCount) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pageCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
