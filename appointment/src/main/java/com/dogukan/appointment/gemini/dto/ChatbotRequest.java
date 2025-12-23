package com.dogukan.appointment.gemini.dto;

import jdk.jfr.SettingDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {
    private String query;
    private String userId;
    private List<MessageHistory> history;
}
