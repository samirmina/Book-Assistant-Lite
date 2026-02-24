package com.bookassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final ObjectMapper mapper;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.base-url:https://ollama.com}")
    private String baseUrl;

    @Value("${app.ai.model:qwen3-coder:480b-cloud}")
    private String model;

    @Value("${app.ai.timeout-seconds:300}")
    private int timeoutSeconds;

    public AiService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String ask(String systemPrompt, String userContent) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", false);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userContent)
        ));
        return call(body);
    }

    public String ask(String systemPrompt, String userContent, String outputLanguage) {
        // Adjust system prompt based on output language
        String adjustedSystemPrompt = systemPrompt;
        if ("en".equals(outputLanguage)) {
            adjustedSystemPrompt = systemPrompt + " Respond in English.";
        } else if ("ar".equals(outputLanguage)) {
            adjustedSystemPrompt = systemPrompt + " أجب باللغة العربية.";
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", false);
        body.put("messages", List.of(
                Map.of("role", "system", "content", adjustedSystemPrompt),
                Map.of("role", "user", "content", userContent)
        ));
        return call(body);
    }

    public String generateComprehensionTest(String text, int startPage, int endPage) {
        String system = "أنت مساعد يكتب اختبار فهم من كتاب PDF. اكتب أسئلة اختيار من متعدد وإجابات نموذجية بالعربية.";
        String user = "نص من الصفحات " + startPage + " إلى " + endPage + ":\n" + text;
        return ask(system, user);
    }

    public String generateComprehensionTest(String text, int startPage, int endPage, String outputLanguage) {
        String system;
        if ("en".equals(outputLanguage)) {
            system = "You are an assistant that creates comprehension tests from a PDF book. Write multiple choice questions and model answers in English.";
        } else {
            system = "أنت مساعد يكتب اختبار فهم من كتاب PDF. اكتب أسئلة اختيار من متعدد وإجابات نموذجية بالعربية.";
        }
        String user = "نص من الصفحات " + startPage + " إلى " + endPage + ":\n" + text;
        return ask(system, user, outputLanguage);
    }

    private String call(Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body);
            log.info("Calling AI API at: {}", baseUrl);
            log.info("Timeout set to: {} seconds", timeoutSeconds);
            log.debug("Request body: {}", json);

            // Create client with proper timeout for this request
            HttpClient requestClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            log.info("Sending request to Ollama Cloud...");
            HttpResponse<String> response = requestClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Response status: {}", response.statusCode());
            log.debug("Response body: {}", response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return extractContent(response.body());
            }
            return "AI error: HTTP " + response.statusCode() + " - " + response.body();
        } catch (IOException e) {
            log.error("IO error calling AI API", e);
            return "AI error: Connection failed - " + e.getMessage();
        } catch (InterruptedException e) {
            log.error("Interrupted while calling AI API", e);
            Thread.currentThread().interrupt();
            return "AI error: Request interrupted";
        } catch (Exception e) {
            log.error("Unexpected error calling AI API", e);
            return "AI error: " + e.getMessage();
        }
    }

    /** استخراج نص الرد من صيغة OpenAI-compatible API: { "choices": [{ "message": { "content": "..." } }] } */
    @SuppressWarnings("unchecked")
    private String extractContent(String jsonBody) {
        try {
            Map<String, Object> root = mapper.readValue(jsonBody, Map.class);
            
            // Try OpenAI-compatible format first (for Ollama Cloud)
            Object choices = root.get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object firstChoice = ((List<?>) choices).get(0);
                if (firstChoice instanceof Map) {
                    Object message = ((Map<String, Object>) firstChoice).get("message");
                    if (message instanceof Map) {
                        Object content = ((Map<String, Object>) message).get("content");
                        if (content != null) {
                            return content.toString();
                        }
                    }
                }
            }
            
            // Fallback to Ollama native format
            Object msg = root.get("message");
            if (msg instanceof Map) {
                Object content = ((Map<String, Object>) msg).get("content");
                return content != null ? content.toString() : jsonBody;
            }
            
            return jsonBody;
        } catch (Exception e) {
            log.error("Error extracting content from response", e);
            return jsonBody;
        }
    }
}

