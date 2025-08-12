package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentUpdate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.model.Appointment;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {
    AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

    DtoAppointmentResponse toDtoAppointmentResponse(Appointment appointment);
    List<DtoAppointmentResponse> toDtoAppointmentResponseList(List<Appointment> appointments);

    Appointment toAppointment(DtoAppointmentCreate dtoAppointmentCreate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAppointmentFromDto(DtoAppointmentUpdate dtoAppointmentUpdate, @MappingTarget Appointment appointment);
}
