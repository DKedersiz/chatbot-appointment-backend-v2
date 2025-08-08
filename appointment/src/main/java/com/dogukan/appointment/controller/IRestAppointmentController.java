package com.dogukan.appointment.controller;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCancel;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.model.RootEntity;

import java.util.List;
import java.util.UUID;

public interface IRestAppointmentController {
    public RootEntity<DtoAppointmentResponse> createAppointment(DtoAppointmentCreate request);
    public RootEntity<DtoAppointmentResponse> cancelAppointment(UUID id, DtoAppointmentCancel cancelRequest);
    public RootEntity<DtoAppointmentResponse> getAppointmentById(UUID id);
    public RootEntity<List<DtoAppointmentResponse>> getAllAppointments();
    public RootEntity<List<DtoAppointmentResponse>> getAllAppointmentsByUserId(UUID id);
    public RootEntity<String> deleteAppointment(UUID id);
}
