package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.dto.user.DtoUser;
import com.dogukan.appointment.dto.user.DtoUserIU;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.mapper.UserMapper;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Validated
@Service
public class UserServiceImpl implements IUserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    @Validated({DtoUserIU.CreateValidation.class, Default.class})
    public DtoUser createUser(@Valid DtoUserIU dtoUser) {
        log.info("Creating new user with email : {}", dtoUser.getEmail());

        if (userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_EXISTS, "Bu e-posta adresi ile kayıtlı kullanıcı var."));
        }

        if (userRepository.existsByTckNo(dtoUser.getTckNo())) {
            throw new BaseException(new ErrorMessage(MessageType.TCKNO_ALREADY_EXISTS, "Bu TC Kimlik numarası ile kayıtlı kullanıcı var."));
        }

        User user = userMapper.fromDtoUserIU(dtoUser);
        user.setPassword(passwordEncoder.encode(dtoUser.getPassword()));
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + email)));
        return userMapper.toDto(user);
    }

    @Override
    public DtoUser getUserByTckNo(String tckNo) {
        log.info("Getting user by TckNo: {}", tckNo);
        User user = userRepository.findByTckNo(tckNo)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + tckNo)));
        return userMapper.toDto(user);
    }

    @Override
    public List<DtoUser> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @Validated(Default.class)
    public DtoUser updateUser(UUID id, @Valid DtoUserIU dtoUser) {
        log.info("Updating user with id : {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + id)));

        if (dtoUser.getEmail() != null && !dtoUser.getEmail().equals(existingUser.getEmail()) && userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_EXISTS, "Bu e-posta adresi zaten kullanımda."));
        }

        userMapper.updateUserFromDto(dtoUser, existingUser);
        if (dtoUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(dtoUser.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully");

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public String deleteUser(UUID id) {
        log.info("Deleting user with id : {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + id)));
        userRepository.delete(user);
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + username)));
    }
}