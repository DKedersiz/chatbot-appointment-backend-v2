package com.dogukan.appointment.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;



    @Test
    void itShouldfindByEmail() {
    }

    @Test
    void itShouldfindByTckNo() {
    }

    @Test
    void existsByEmail() {
    }

    @Test
    void existsByTckNo() {
    }

    @Test
    void findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase() {
    }
}