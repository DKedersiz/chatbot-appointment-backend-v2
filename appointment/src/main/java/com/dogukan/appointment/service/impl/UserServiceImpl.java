package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.dto.DtoUser;
import com.dogukan.appointment.dto.DtoUserIU;
import com.dogukan.appointment.enums.Role;
import com.dogukan.appointment.mapper.UserMapper;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.DuplicateFormatFlagsException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Validated
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,UserMapper userMapper,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public DtoUser createUser(@Valid DtoUserIU dtoUser) {
        log.info("Creating new user with email : {}", dtoUser.getEmail());

        if(userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new DuplicateFormatFlagsException("Bu e-posta adresi ile kayıtlı kullanıcı var.");
        }

        if(userRepository.existsByTckNo(dtoUser.getTckNo())) {
            throw new DuplicateFormatFlagsException("Bu TC Kimlik numarası ile kayıtlı kullanıcı var.");
        }

        User user = User.builder()
                .tckNo(dtoUser.getTckNo())
                .firstName(dtoUser.getFirstName())
                .lastName(dtoUser.getLastName())
                .email(dtoUser.getEmail())
                .address(dtoUser.getAddress())
                .phoneNumber(dtoUser.getPhoneNumber())
                .gender(dtoUser.getGender())
                .password(passwordEncoder.encode(dtoUser.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .isVerified(false)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User successfully created");

        return userMapper.toDto(savedUser);
    }

    @Override
    public Optional<DtoUser> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Override
    public DtoUser getUserByEmail(String email) {
        Optional<User> foundUser = userRepository.findByEmail(email);
        return userMapper.toDto(foundUser.get());
    }

    @Override
    public DtoUser getUserByTckNo(String tckNo) {
        log.info("Getting user by TckNo: {}", tckNo);
        Optional<User> foundUser = userRepository.findByTckNo(tckNo);
        return userMapper.toDto(foundUser.get());
    }

    @Override
    public List<DtoUser> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional
    public DtoUser updateUser(UUID id,DtoUserIU dtoUser) {
        log.info("Updating user with id : {}", id);

        Optional<User> existingUser = userRepository.findById(id);

        if(dtoUser.getEmail() != null && !dtoUser.getEmail().equals(existingUser.get().getEmail()) && userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kullanımda.");
        }

        if(dtoUser.getFirstName() != null) {
            existingUser.get().setFirstName(dtoUser.getFirstName());
        }

        if(dtoUser.getLastName() != null) {
            existingUser.get().setLastName(dtoUser.getLastName());
        }

        if(dtoUser.getAddress() != null) {
            existingUser.get().setAddress(dtoUser.getAddress());
        }

        if(dtoUser.getPhoneNumber() != null) {
            existingUser.get().setPhoneNumber(dtoUser.getPhoneNumber());
        }

        if(dtoUser.getGender() != null) {
            existingUser.get().setGender(dtoUser.getGender());
        }

        if(dtoUser.getEmail() != null) {
            existingUser.get().setEmail(dtoUser.getEmail());
        }

        User updatedUser = userRepository.save(existingUser.get());
        log.info("User updated successfully");

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public String deleteUser(UUID id) {
        log.info("Deleting user with id : {}", id);

        User user = userRepository.findById(id).orElse(null);
        if(user != null) {
            userRepository.delete(user);
        }
        return "Kullanıcı başarıyla silindi.";
    }

    @Override
    public List<DtoUser> searchUsersByName(String firstName, String lastName) {
        log.debug("Searching users by name : {}", firstName + " " + lastName);

        List<User> users = userRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                firstName != null ? firstName : "",
                lastName != null ? lastName : ""
        );
        return userMapper.toDtoList(users);
    }
}
