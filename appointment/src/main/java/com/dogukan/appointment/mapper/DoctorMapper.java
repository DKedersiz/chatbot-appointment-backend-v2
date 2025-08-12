package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.doctor.DtoDoctorCreate;
import com.dogukan.appointment.dto.doctor.DtoDoctorResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorUpdate;
import com.dogukan.appointment.model.Doctor;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {
    DoctorMapper INSTANCE = Mappers.getMapper(DoctorMapper.class);

    DtoDoctorResponse toDtoDoctorResponse(Doctor doctor);
    List<DtoDoctorResponse> toDtoDoctorResponseList(List<Doctor> doctors);

    Doctor toDoctor(DtoDoctorCreate dtoDoctorCreate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDoctorFromDto(DtoDoctorUpdate dtoDoctorUpdate, @MappingTarget Doctor doctor);
}
