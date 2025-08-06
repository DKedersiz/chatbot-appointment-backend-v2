package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.model.ChatMessage;
import com.dogukan.appointment.repository.ChatMessageRepository;
import com.dogukan.appointment.service.IChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatMessageServiceImpl implements IChatMessageService {

    private final ChatMessageRepository _chatMessageRepository;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository) {
        this._chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        return _chatMessageRepository.save(chatMessage);
    }

    @Override
    public Optional<ChatMessage> findById(UUID id) {
        return _chatMessageRepository.findById(id);
    }

    @Override
    public List<ChatMessage> findAll() {
        return _chatMessageRepository.findAll();
    }

    @Override
    public List<ChatMessage> findBySessionId(UUID sessionId) {
        return _chatMessageRepository.findByChatSession_ChatSessionId(sessionId);
    }

    @Override
    public void deleteById(UUID id) {
        _chatMessageRepository.deleteById(id);
    }
}
