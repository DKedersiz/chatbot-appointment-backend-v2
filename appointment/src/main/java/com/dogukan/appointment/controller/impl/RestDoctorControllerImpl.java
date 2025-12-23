package com.dogukan.appointment.controller.impl;

import com.dogukan.appointment.controller.IRestDoctorController;
import com.dogukan.appointment.dto.doctor.DtoDoctorCreate;
import com.dogukan.appointment.dto.doctor.DtoDoctorListResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorUpdate;
import com.dogukan.appointment.model.RootEntity;
import com.dogukan.appointment.service.IDoctorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/rest/api/doctors")
public class RestDoctorControllerImpl implements IRestDoctorController {

    private final IDoctorService doctorService;

    public RestDoctorControllerImpl(IDoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public RootEntity<DtoDoctorResponse> createDoctor(@Valid @RequestBody DtoDoctorCreate request) {
        return RootEntity.ok(doctorService.createDoctor(request));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public RootEntity<DtoDoctorResponse> updateDoctor(@PathVariable UUID id,@Valid
    @RequestBody DtoDoctorUpdate request) {
        return RootEntity.ok(doctorService.updateDoctor(id,request));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public RootEntity<DtoDoctorResponse> getDoctorById(@PathVariable UUID id) {
        return RootEntity.ok(doctorService.getDoctorById(id));
    }

    @Override
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public RootEntity<List<DtoDoctorListResponse>> getAllDoctors() {
        return RootEntity.ok(doctorService.getAllDoctors());
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RootEntity<String> deleteDoctor(@PathVariable UUID id) {
        return RootEntity.ok(doctorService.deleteDoctor(id));
    }
}
