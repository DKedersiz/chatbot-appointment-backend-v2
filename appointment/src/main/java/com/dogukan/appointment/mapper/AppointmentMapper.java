package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.model.Appointment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    DtoAppointmentResponse toDtoAppointmentResponse(Appointment appointment);
    Appointment toAppointment(DtoAppointmentCreate dtoAppointmentCreate);
    List<DtoAppointmentResponse> toDtoAppointmentResponseList(List<Appointment> appointments);

}
