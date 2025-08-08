package com.dogukan.appointment.model;

import com.dogukan.appointment.enums.SenderType;
import com.dogukan.appointment.model.Appointment;
import com.dogukan.appointment.model.ChatSession;
import com.dogukan.appointment.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_patient", columnList = "user_id"),
                @Index(name = "idx_chat_appointment", columnList = "appointment_id"),
                @Index(name = "idx_chat_timestamp", columnList = "sent_at")
        })
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID messageId;

    @NotNull(message = "Mesajı gönderen hasta bilgisi gereklidir.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @NotNull(message = "Sohbet oturumu bilgisi gereklidir.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;

    @NotBlank(message = "Mesaj içeriği boş olamaz.")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    @Builder.Default
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}
