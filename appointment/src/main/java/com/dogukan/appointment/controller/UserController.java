package com.dogukan.appointment.controller;

import com.dogukan.appointment.dto.user.DtoUser;
import com.dogukan.appointment.dto.user.DtoUserIU;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.security.UserPrincipal;
import com.dogukan.appointment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/rest/api/users")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/create")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<DtoUser> createUser(@Valid @RequestBody DtoUserIU dtoUserIU) {
        return RootEntity.ok(userService.createUser(dtoUserIU));
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.principal.userId.toString()")
    public RootEntity<Optional<DtoUser>> getUserById(@PathVariable UUID id) {
        return RootEntity.ok(userService.getUserById(id));
    }

    @GetMapping(path = "/tckNo/{tckNo}")
    @PreAuthorize("hasRole('ADMIN') or principal.tckNo == #tckNo")
    public RootEntity<DtoUser> getUserByTckNo(@PathVariable String tckNo) {
        return RootEntity.ok(userService.getUserByTckNo(tckNo));
    }

    @GetMapping(path = "/all")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<List<DtoUser>> getAllUsers() {
        return RootEntity.ok(userService.getAllUsers());
    }

    @PutMapping(path = "/update/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or #uuid.toString() == authentication.principal.userId.toString()")
    public RootEntity<DtoUser> updateUser(@Valid @RequestBody DtoUserIU dtoUserIU, @PathVariable UUID uuid,
                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        return RootEntity.ok(userService.updateUser(uuid, dtoUserIU, currentUser));
    }

    @DeleteMapping(path = "/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<String> deleteUser(@PathVariable UUID id) {
        return RootEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<List<DtoUser>> searchUsersByName(@RequestParam String firstName, @RequestParam String lastName) {
        return RootEntity.ok(userService.searchUsersByName(firstName, lastName));
    }
}