package com.dogukan.appointment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "refresh_token_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expired_date")
    private Date expiredDate;

    @ManyToOne
    private User user;

}
