package com.dogukan.appointment.mapper;

import com.dogukan.appointment.dto.doctor.DtoDoctorCreate;
import com.dogukan.appointment.dto.doctor.DtoDoctorResponse;
import com.dogukan.appointment.model.Doctor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    DtoDoctorResponse  toDtoDoctorResponse(Doctor doctor);
    Doctor toDoctor(DtoDoctorCreate dtoDoctorCreate);
    List<DtoDoctorResponse> toDtoDoctorResponseList(List<Doctor> doctors);
}
