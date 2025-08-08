package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCancel;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;

import java.util.List;
import java.util.UUID;

public interface IAppointmentService {
    public DtoAppointmentResponse createAppointment(DtoAppointmentCreate appointmentCreate);
    public DtoAppointmentResponse cancelAppointment(UUID id,DtoAppointmentCancel appointmentCancel);
    public DtoAppointmentResponse getAppointmentById(UUID id);
    public List<DtoAppointmentResponse> getAllAppointments();
    public List<DtoAppointmentResponse> getAllAppointmentsByUserId(UUID id);
    public String deleteAppointment(UUID id);

}
