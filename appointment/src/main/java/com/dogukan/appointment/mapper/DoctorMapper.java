package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.doctor.DtoDoctorResponse;
import com.dogukan.appointment.model.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "departmentId", source = "department.departmentId")
    DtoDoctorResponse toDtoDoctorResponse(Doctor doctor);
}
