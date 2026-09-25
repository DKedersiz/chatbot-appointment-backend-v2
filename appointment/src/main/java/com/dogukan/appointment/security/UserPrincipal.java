package com.dogukan.appointment.security;

import com.dogukan.appointment.enums.Role;
import com.dogukan.appointment.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

// Spring Security'nin gördüğü tek şey bu olmalı: User entity'sinin kendisi değil,
// login anında alınmış, dar kapsamlı ve değişmez bir anlık görüntüsü.
public class UserPrincipal implements UserDetails {

    private final UUID userId;
    private final String tckNo;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final Role role;
    private final boolean active;
    private final boolean verified;
    private final LocalDateTime lockedUntil;

    private UserPrincipal(User user) {
        this.userId = user.getUserId();
        this.tckNo = user.getTckNo();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.active = user.getIsActive();
        this.verified = user.getIsVerified();
        this.lockedUntil = user.getLockedUntil();
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTckNo() {
        return tckNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active && (lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now()));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active && verified;
    }
}
