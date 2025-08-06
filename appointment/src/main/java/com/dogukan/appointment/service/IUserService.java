package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.DtoUser;
import com.dogukan.appointment.dto.DtoUserIU;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserService {
    DtoUser createUser(@Valid DtoUserIU dtoUser);
    Optional<DtoUser> getUserById(UUID id);
    DtoUser getUserByEmail(String email);
    DtoUser getUserByTckNo(String tckNo);
    List<DtoUser> getAllUsers();
    DtoUser updateUser(UUID id,DtoUserIU user);
    void deleteUser(UUID id);

    List<DtoUser> searchUsersByName(String firstName, String lastName);

}
