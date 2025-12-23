package com.dogukan.appointment.gemini;

import com.dogukan.appointment.gemini.dto.MessageHistory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiServiceImpl {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    private Map<String, Object> createResponseSchemaContent() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "OBJECT");

        Map<String, Object> properties = new HashMap<>();

        // intent
        properties.put("intent", Map.of(
                "type", "STRING",
                "description", "RANDEVU_ARA, RANDEVU_OLUSTUR veya BILGI_YOK"
        ));

        // department
        properties.put("department", Map.of(
                "type", "STRING",
                "description", "Kullanıcının istediği bölüm adı. Eksikse 'null'."
        ));

        // doctor
        properties.put("doctor", Map.of(
                "type", "STRING",
                "description", "Kullanıcının istediği doktor adı. Eksikse 'null'."
        ));

        // date
        properties.put("date", Map.of(
                "type", "STRING",
                "description", "YYYY-MM-DD formatında hesaplanmış tarih. Eksikse 'null'."
        ));

        // time
        properties.put("time", Map.of(
                "type", "STRING",
                "description", "HH:mm formatında saat. Eksikse 'null'."
        ));

        // missing_info
        properties.put("missing_info", Map.of(
                "type", "STRING",
                "description", "Randevu için gerekli bilgi eksikse kullanıcıya sorulacak soru metni. Eksik yoksa 'null'."
        ));

        schema.put("properties", properties);
        return schema;
    }

    public String extractIntentAndEntities(String userMessage, String availableDepartments, List<MessageHistory> history) {
        String rawResponseText = "";
        try {
            String currentDate = LocalDate.now().toString();
            String currentDay = LocalDate.now().getDayOfWeek().toString();

            String systemPrompt = String.format("""
                Sen bir randevu asistanısın. Konuşma geçmişini ve mevcut müsait bölümleri (%s) dikkate alarak, kullanıcının niyetini (intent), bölüm, tarih ve saat bilgilerini ÇIKAR. ASLA başka metin üretme.
                
                Şu anki tarih: %s.
                """, availableDepartments, currentDate);


            List<Map<String, Object>> contents = new ArrayList<>();
            boolean firstMessage = true;

            for (MessageHistory msg : history) {
                String role = msg.getSender().equalsIgnoreCase("user") ? "user" : "model";
                String text = msg.getText();

                if (firstMessage && role.equals("user")) {
                    contents.add(Map.of(
                            "role", role,
                            "parts", new Object[]{Map.of("text", systemPrompt + "\n\nKullanıcı Mesajı: " + text)}
                    ));
                    firstMessage = false;
                } else {
                    contents.add(Map.of(
                            "role", role,
                            "parts", new Object[]{Map.of("text", text)}
                    ));
                    firstMessage = false;
                }
            }

            if (contents.isEmpty() && !userMessage.isEmpty()) {
                contents.add(Map.of(
                        "role", "user",
                        "parts", new Object[]{Map.of("text", systemPrompt + "\n\nKullanıcı Mesajı: " + userMessage)}
                ));
            }


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("responseSchema", createResponseSchemaContent());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = API_URL + "?key=" + apiKey;
            String response = restTemplate.postForObject(url, entity, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode parts = root.path("candidates").get(0).path("content").path("parts");

            if (parts.size() > 0 && parts.get(0).has("text")) {
                rawResponseText = parts.get(0).path("text").asText();

                objectMapper.readTree(rawResponseText);
                return rawResponseText;

            } else {
                throw new Exception("AI'dan yanıt gelmedi veya yanıt formatı hatalı.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"intent\": " + "\"ERROR\"" + ", \"missing_info\": \"Üzgünüm, AI entegrasyonunda bir sorun oluştu. Detay: " + e.getMessage() + ". Raw Response: " + rawResponseText + "\"}";
        }
    }
}