package com.dogukan.appointment.controller.impl;

import com.dogukan.appointment.controller.IRestUserController;
import com.dogukan.appointment.dto.user.DtoUser;
import com.dogukan.appointment.dto.user.DtoUserIU;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping(path = "/create")
    public RootEntity<DtoUser> createUser(@Valid @RequestBody DtoUserIU dtoUserIU) {
        return RootEntity.ok(userService.createUser(dtoUserIU));
    }

    @Override
    @GetMapping(path = "/getUserById/{id}")
    public RootEntity<Optional<DtoUser>> getUserById(@PathVariable UUID id) {
        return RootEntity.ok(userService.getUserById(id));
    }

    @Override
    @GetMapping(path = "/getUserByTckNo/{tckNo}")
    public RootEntity<DtoUser> getUserByTckNo(@PathVariable String tckNo) {
        return RootEntity.ok(userService.getUserByTckNo(tckNo));
    }

    @Override
    @GetMapping(path = "/getAllUsers")
    public RootEntity<List<DtoUser>> getAllUsers() {
        return RootEntity.ok(userService.getAllUsers());
    }

    @Override
    @PutMapping(path = "/updateUser/{uuid}")
    public RootEntity<DtoUser> updateUser(@Valid @RequestBody DtoUserIU dtoUserIU,@PathVariable UUID uuid) {
        return RootEntity.ok(userService.updateUser(uuid,dtoUserIU));
    }

    @Override
    @DeleteMapping(path = "/delete/{id}")
    public RootEntity<String> deleteUser(@PathVariable UUID id) {
        return RootEntity.ok(userService.deleteUser(id));
    }

    @Override
    @GetMapping("/searchUsersByName/{firstName}+{lastName}")
    public RootEntity<List<DtoUser>> searchUsersByName(@PathVariable String firstName, @PathVariable String lastName) {
        return RootEntity.ok(userService.searchUsersByName(firstName,lastName));
    }
}
