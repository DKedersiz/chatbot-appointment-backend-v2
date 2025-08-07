package com.dogukan.appointment.controller;

import com.dogukan.appointment.dto.DtoUser;
import com.dogukan.appointment.dto.DtoUserIU;
import com.dogukan.appointment.model.RootEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRestUserController {

    public RootEntity<DtoUser> createUser(DtoUserIU dtoUserIU);
    public RootEntity<Optional<DtoUser>> getUserById(UUID id);
    public RootEntity<DtoUser> getUserByTckNo(String tckNo);
    public RootEntity<List<DtoUser>> getAllUsers();
    public RootEntity<DtoUser> updateUser(DtoUserIU dtoUserIU, UUID uuid);
    public RootEntity<String> deleteUser(UUID id);
    public RootEntity<List<DtoUser>> searchUsersByName(String firstName, String lastName);
}
