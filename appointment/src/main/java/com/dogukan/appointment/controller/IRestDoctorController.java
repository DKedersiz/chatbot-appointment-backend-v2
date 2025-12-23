package com.dogukan.appointment.controller;

import com.dogukan.appointment.dto.doctor.DtoDoctorCreate;
import com.dogukan.appointment.dto.doctor.DtoDoctorListResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorUpdate;
import com.dogukan.appointment.model.RootEntity;

import java.util.List;
import java.util.UUID;

public interface IRestDoctorController {
    public RootEntity<DtoDoctorResponse> createDoctor(DtoDoctorCreate request);
    public RootEntity<DtoDoctorResponse> updateDoctor(UUID id, DtoDoctorUpdate request);
    public RootEntity<DtoDoctorResponse> getDoctorById(UUID id);
    public RootEntity<List<DtoDoctorListResponse>> getAllDoctors();
    public RootEntity<String> deleteDoctor(UUID uuid);

}
