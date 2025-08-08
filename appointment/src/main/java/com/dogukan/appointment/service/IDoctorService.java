package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.doctor.DtoDoctorCreate;
import com.dogukan.appointment.dto.doctor.DtoDoctorResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorUpdate;

import java.util.List;
import java.util.UUID;

public interface IDoctorService {
    DtoDoctorResponse createDoctor(DtoDoctorCreate request);
    DtoDoctorResponse updateDoctor(UUID id, DtoDoctorUpdate request);
    DtoDoctorResponse getDoctorById(UUID id);
    List<DtoDoctorResponse> getAllDoctors();
    String deleteDoctor(UUID id);

}
