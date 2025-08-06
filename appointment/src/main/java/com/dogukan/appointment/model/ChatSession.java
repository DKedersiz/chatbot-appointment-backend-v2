package com.dogukan.appointment.model;

import com.dogukan.appointment.enums.ChatSessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_session_user", columnList = "user_id"),
        @Index(name = "idx_chat_session_status", columnList = "status"),
        @Index(name = "idx_chat_session_started", columnList = "started_at"),
        @Index(name = "idx_chat_session_appointment", columnList = "appointment_id")
})
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @NotNull(message = "Kullanıcı bilgisi gereklidir.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false, nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChatSessionStatus status = ChatSessionStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Size(max = 500, message = "Özet 500 karakterden uzun olamaz.")
    @Column(name = "session_summary", columnDefinition = "TEXT")
    private String sessionSummary;

    @Size(max = 100, message = "AI model adı 100 karakterden uzun olamaz.")
    @Column(name = "ai_model_used")
    private String aiModelUsed;

    @Column(name = "total_messages")
    private Integer totalMessages = 0;

    @Column(name = "total_tokens_used")
    private Integer totalTokensUsed = 0;

    @Size(max = 200, message = "Kullanıcı agent bilgisi 200 karakterden uzun olamaz.")
    @Column(name = "user_agent")
    private String userAgent;

    @Size(max = 45, message = "IP adresi 45 karakterden uzun olamaz.")
    @Column(name = "ip_address")
    private String ipAddress;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    // Utility methods
    public void endSession() {
        this.status = ChatSessionStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
    }

    public void abandonSession() {
        this.status = ChatSessionStatus.ABANDONED;
        this.endedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == ChatSessionStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return status == ChatSessionStatus.COMPLETED;
    }

    public long getDurationInMinutes() {
        LocalDateTime end = endedAt != null ? endedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, end).toMinutes();
    }

    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }

    public void incrementMessageCount() {
        this.totalMessages = getMessageCount() + 1;
    }

    public void addTokensUsed(int tokens) {
        if (this.totalTokensUsed == null) {
            this.totalTokensUsed = 0;
        }
        this.totalTokensUsed += tokens;
    }

    public boolean hasAppointment() {
        return appointment != null;
    }

    public String getUserFullName() {
        return user != null ? user.getFullName() : null;
    }

}