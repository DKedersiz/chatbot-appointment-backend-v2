package com.dogukan.appointment.gemini.api;

import com.dogukan.appointment.gemini.ChatbotServiceImpl;
import com.dogukan.appointment.gemini.dto.ChatbotRequest;
import com.dogukan.appointment.gemini.dto.ChatbotResponse;
import com.dogukan.appointment.model.RootEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/api/chatbot")
@CrossOrigin(origins= "http://localhost:3000",allowCredentials = "true")
public class RestChatbotController {

    private final ChatbotServiceImpl chatbotService;

    public RestChatbotController(ChatbotServiceImpl chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/appointment")
    public RootEntity<ChatbotResponse> chat(@RequestBody ChatbotRequest request) {
        return RootEntity.ok(chatbotService.processMessage(request));
    }

}
