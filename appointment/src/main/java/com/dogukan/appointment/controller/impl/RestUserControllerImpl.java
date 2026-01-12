package com.dogukan.appointment.controller.impl;

import com.dogukan.appointment.controller.IRestUserController;
import com.dogukan.appointment.dto.user.DtoUser;
import com.dogukan.appointment.dto.user.DtoUserIU;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
// Frontend ile uyumlu olması için yolu 'users' olarak güncelledik
@RequestMapping(path = "/rest/api/users")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RestUserControllerImpl implements IRestUserController {

    private final IUserService userService;

    public RestUserControllerImpl(IUserService userService) {
        this.userService = userService;
    }

    @Override
    @PostMapping(path = "/create")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<DtoUser> createUser(@Valid @RequestBody DtoUserIU dtoUserIU) {
        return RootEntity.ok(userService.createUser(dtoUserIU));
    }

    @Override
    @GetMapping(path = "/{id}")
    // Kritik Düzeltme: Kullanıcı sadece KENDİ id'si ile istek atıyorsa veya ADMIN ise izin ver
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.principal.userId.toString()")
    public RootEntity<Optional<DtoUser>> getUserById(@PathVariable UUID id) {
        return RootEntity.ok(userService.getUserById(id));
    }

    @Override
    @GetMapping(path = "/tckNo/{tckNo}")
    @PreAuthorize("hasRole('ADMIN') or principal.tckNo == #tckNo")
    public RootEntity<DtoUser> getUserByTckNo(@PathVariable String tckNo) {
        return RootEntity.ok(userService.getUserByTckNo(tckNo));
    }

    @Override
    @GetMapping(path = "/all")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<List<DtoUser>> getAllUsers() {
        return RootEntity.ok(userService.getAllUsers());
    }

    @Override
    @PutMapping(path = "/update/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or #uuid.toString() == authentication.principal.userId.toString()")
    public RootEntity<DtoUser> updateUser(@Valid @RequestBody DtoUserIU dtoUserIU, @PathVariable UUID uuid) {
        return RootEntity.ok(userService.updateUser(uuid, dtoUserIU));
    }

    @Override
    @DeleteMapping(path = "/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<String> deleteUser(@PathVariable UUID id) {
        return RootEntity.ok(userService.deleteUser(id));
    }

    @Override
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<List<DtoUser>> searchUsersByName(@RequestParam String firstName, @RequestParam String lastName) {
        return RootEntity.ok(userService.searchUsersByName(firstName, lastName));
    }
}