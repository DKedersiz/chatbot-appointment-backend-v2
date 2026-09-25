package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.user.DtoUser;
import com.dogukan.appointment.dto.user.DtoUserIU;
import com.dogukan.appointment.enums.Role;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.mapper.UserMapper;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.security.UserPrincipal;
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
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

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

    public Optional<DtoUser> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    public DtoUser getUserByTckNo(String tckNo) {
        log.info("Getting user by TckNo: {}", tckNo);
        User user = userRepository.findByTckNo(tckNo)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + tckNo)));
        return userMapper.toDto(user);
    }

    public List<DtoUser> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    @Validated(Default.class)
    public DtoUser updateUser(UUID id, @Valid DtoUserIU dtoUser, UserPrincipal currentUser) {
        log.info("Updating user with id : {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + id)));

        if (dtoUser.getEmail() != null && !dtoUser.getEmail().equals(existingUser.getEmail()) && userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_ALREADY_EXISTS, "Bu e-posta adresi zaten kullanımda."));
        }

        if (dtoUser.getRole() != null && dtoUser.getRole() != existingUser.getRole()) {
            if (currentUser.getRole() != Role.ADMIN) {
                throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED_REQUEST, "Rol değişikliği yalnızca yöneticiler tarafından yapılabilir."));
            }
            existingUser.setRole(dtoUser.getRole());
        }

        userMapper.updateUserFromDto(dtoUser, existingUser);
        if (dtoUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(dtoUser.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully");

        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public String deleteUser(UUID id) {
        log.info("Deleting user with id : {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + id)));
        user.setIsActive(false);
        userRepository.save(user);
        return "Kullanıcı pasif hale getirildi.";
    }

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
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + username)));
        return UserPrincipal.from(user);
    }
}