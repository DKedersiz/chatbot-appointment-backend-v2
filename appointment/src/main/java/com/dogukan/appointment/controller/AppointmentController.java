package com.dogukan.appointment.controller;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCancel;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.security.UserPrincipal;
import com.dogukan.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/rest/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public RootEntity<DtoAppointmentResponse> createAppointment(@Valid @RequestBody DtoAppointmentCreate request,
                                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        return RootEntity.ok(appointmentService.createAppointment(request, currentUser));
    }


    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public RootEntity<DtoAppointmentResponse> cancelAppointment(@PathVariable UUID id, @Valid @RequestBody DtoAppointmentCancel request,
                                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        return RootEntity.ok(appointmentService.cancelAppointment(id, request, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public RootEntity<DtoAppointmentResponse> getAppointmentById(@PathVariable UUID id,
                                                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        return RootEntity.ok(appointmentService.getAppointmentById(id, currentUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<List<DtoAppointmentResponse>> getAllAppointments() {
        return RootEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #userId.toString() == authentication.principal.userId.toString())")
    public RootEntity<List<DtoAppointmentResponse>> getAllAppointmentsByUserId(@PathVariable UUID userId) {
        return RootEntity.ok(appointmentService.getAllAppointmentsByUserId(userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<String> deleteAppointment(@PathVariable UUID id) {
        return RootEntity.ok(appointmentService.deleteAppointment(id));
    }
}