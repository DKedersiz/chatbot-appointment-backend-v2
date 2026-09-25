package com.dogukan.appointment.gemini;

import com.dogukan.appointment.gemini.dto.MessageHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiServiceImpl {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private static final int MAX_HISTORY_MESSAGES = 10;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    private Map<String, Object> createResponseSchemaContent() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "OBJECT");
        Map<String, Object> properties = new HashMap<>();
        properties.put("intent", Map.of("type", "STRING", "description", "RANDEVU_ARA, RANDEVU_OLUSTUR, SOHBET"));
        properties.put("department", Map.of("type", "STRING"));
        properties.put("doctor_name", Map.of("type", "STRING", "description", "Randevu oluşturulacak doktorun tam adı."));
        properties.put("date", Map.of("type", "STRING", "description", "YYYY-MM-DD"));
        properties.put("time", Map.of("type", "STRING", "description", "HH:mm"));
        properties.put("complaint", Map.of("type", "STRING", "description", "Kullanıcının şikayeti."));
        properties.put("chat_response", Map.of("type", "STRING", "description", "Kullanıcıya verilecek yanıt."));
        schema.put("properties", properties);
        schema.put("required", List.of("intent", "chat_response"));
        return schema;
    }

    public String extractIntentAndEntities(String userQuery, String hospitalContext, String availableSlots, List<MessageHistory> history) {
        try {
            String systemPrompt = hospitalContext + "\n" + availableSlots + "\nYANITINI MUTLAKA JSON OLARAK VER.";
            List<Map<String, Object>> contents = new ArrayList<>();
            List<MessageHistory> recentHistory = history.subList(Math.max(0, history.size() - MAX_HISTORY_MESSAGES), history.size());
            for (MessageHistory h : recentHistory) {
                contents.add(Map.of("role", h.getSender().equalsIgnoreCase("user") ? "user" : "model",
                        "parts", List.of(Map.of("text", h.getText()))));
            }
            contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", systemPrompt + "\nKullanıcı: " + userQuery))));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);
            requestBody.put("generationConfig", Map.of("responseMimeType", "application/json", "responseSchema", createResponseSchemaContent()));

            String response = restTemplate.postForObject(API_URL + "?key=" + apiKey, new HttpEntity<>(requestBody, new HttpHeaders()), String.class);
            return objectMapper.readTree(response).path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Gemini API çağrısı başarısız", e);
            return "{\"intent\": \"SOHBET\", \"chat_response\": \"Sistem geçici olarak yanıt veremiyor.\"}";
        }
    }
}