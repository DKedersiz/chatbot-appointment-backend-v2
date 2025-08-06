package com.dogukan.appointment.service;

import com.dogukan.appointment.model.ChatMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IChatMessageService {
    ChatMessage save(ChatMessage chatMessage);
    Optional<ChatMessage> findById(UUID id);
    List<ChatMessage> findAll();
    List<ChatMessage> findBySessionId(UUID sessionId);
    void deleteById(UUID id);
}
