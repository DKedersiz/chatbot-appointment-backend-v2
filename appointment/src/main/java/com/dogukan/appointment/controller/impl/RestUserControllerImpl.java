package com.dogukan.appointment.controller.impl;

import com.dogukan.appointment.controller.IRestUserController;
import com.dogukan.appointment.dto.user.DtoUser;
import com.dogukan.appointment.dto.DtoUserIU;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.service.IUserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/rest/api/user")
public class RestUserControllerImpl implements IRestUserController {

    private final IUserService userService;

    public RestUserControllerImpl(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public RootEntity<DtoUser> createUser(DtoUserIU dtoUserIU) {
        return RootEntity.ok(userService.createUser(dtoUserIU));
    }

    @Override
    public RootEntity<Optional<DtoUser>> getUserById(UUID id) {
        return RootEntity.ok(userService.getUserById(id));
    }

    @Override
    public RootEntity<DtoUser> getUserByTckNo(String tckNo) {
        return RootEntity.ok(userService.getUserByTckNo(tckNo));
    }

    @Override
    public RootEntity<List<DtoUser>> getAllUsers() {
        return RootEntity.ok(userService.getAllUsers());
    }

    @Override
    public RootEntity<DtoUser> updateUser(DtoUserIU dtoUserIU, UUID uuid) {
        return RootEntity.ok(userService.updateUser(uuid,dtoUserIU));
    }

    @Override
    public RootEntity<String> deleteUser(UUID id) {
        return RootEntity.ok(userService.deleteUser(id));
    }

    @Override
    public RootEntity<List<DtoUser>> searchUsersByName(String firstName, String lastName) {
        return RootEntity.ok(userService.searchUsersByName(firstName,lastName));
    }
}
