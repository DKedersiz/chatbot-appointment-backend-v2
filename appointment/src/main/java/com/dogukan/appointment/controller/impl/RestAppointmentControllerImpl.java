package com.dogukan.appointment.controller.impl;

import com.dogukan.appointment.controller.IRestAppointmentController;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCancel;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.service.IAppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/rest/api/appointments")
public class RestAppointmentControllerImpl implements IRestAppointmentController {

    private final IAppointmentService appointmentService;

    public RestAppointmentControllerImpl(IAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public RootEntity<DtoAppointmentResponse> createAppointment(@Valid @RequestBody DtoAppointmentCreate request) {
        return RootEntity.ok(appointmentService.createAppointment(request));
    }


    @Override
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public RootEntity<DtoAppointmentResponse> cancelAppointment(@PathVariable UUID id, @Valid @RequestBody DtoAppointmentCancel request) {
        return RootEntity.ok(appointmentService.cancelAppointment(id,request));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public RootEntity<DtoAppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        return RootEntity.ok(appointmentService.getAppointmentById(id));
    }

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<List<DtoAppointmentResponse>> getAllAppointments() {
        return RootEntity.ok(appointmentService.getAllAppointments());
    }

    @Override
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #userId.toString() == authentication.principal.userId.toString())")
    public RootEntity<List<DtoAppointmentResponse>> getAllAppointmentsByUserId(@PathVariable UUID userId) {
        return RootEntity.ok(appointmentService.getAllAppointmentsByUserId(userId));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<String> deleteAppointment(@PathVariable UUID id) {
        return RootEntity.ok(appointmentService.deleteAppointment(id));
    }
}