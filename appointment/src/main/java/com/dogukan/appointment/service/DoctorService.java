package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.doctor.DtoDoctorCreate;
import com.dogukan.appointment.dto.doctor.DtoDoctorListResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorResponse;
import com.dogukan.appointment.dto.doctor.DtoDoctorUpdate;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.mapper.DoctorMapper;
import com.dogukan.appointment.model.Department;
import com.dogukan.appointment.model.Doctor;
import com.dogukan.appointment.model.User;
import com.dogukan.appointment.repository.DepartmentRepository;
import com.dogukan.appointment.repository.DoctorRepository;
import com.dogukan.appointment.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorMapper doctorMapper;

    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository,
                             DepartmentRepository departmentRepository, DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorMapper = doctorMapper;
    }

    public DtoDoctorResponse createDoctor(DtoDoctorCreate request) {

        var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND,"Kullanıcı bulunamadı." + request.getUserId())));

        var department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DEPARTMENT_NOT_FOUND,"Bölüm bulunamadı:" + request.getDepartmentId())));

        Doctor doctor = Doctor.builder()
                .user(user)
                .department(department)
                .title(request.getTitle())
                .specialization(request.getSpecialization())
                .experienceYears(request.getExperienceYears())
                .about(request.getAbout())
                .workEmail(request.getWorkEmail())
                .workPhone(request.getWorkPhone())
                .hireDate(request.getHireDate())
                .isActive(request.getIsActive())
                .isAvailable(request.getIsAvailable())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDtoDoctorResponse(savedDoctor);
    }

    public DtoDoctorResponse updateDoctor(UUID id, DtoDoctorUpdate request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_FOUND,"Doktor bulunamadı:" + id)));

        if (request.getDepartmentId() != null) {
            var department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DEPARTMENT_NOT_FOUND, "Bölüm bulunamadı: " + request.getDepartmentId())));
            doctor.setDepartment(department);
        }
        if (request.getTitle() != null) doctor.setTitle(request.getTitle());
        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getAbout() != null) doctor.setAbout(request.getAbout());
        if (request.getWorkPhone() != null) doctor.setWorkPhone(request.getWorkPhone());
        if (request.getWorkEmail() != null) doctor.setWorkEmail(request.getWorkEmail());
        if (request.getHireDate() != null) doctor.setHireDate(request.getHireDate());
        if (request.getIsAvailable() != null) doctor.setIsAvailable(request.getIsAvailable());
        if (request.getIsActive() != null) doctor.setIsActive(request.getIsActive());
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDtoDoctorResponse(updatedDoctor);
    }

    public DtoDoctorResponse getDoctorById(UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_FOUND,"Doktor bulunamadı:" + id)));
        return doctorMapper.toDtoDoctorResponse(doctor);
    }

    public List<DtoDoctorListResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctor -> {
                    User user = doctor.getUser();
                    Department dept = doctor.getDepartment();

                    DtoDoctorListResponse dto = new DtoDoctorListResponse();
                    dto.setDoctorId(doctor.getDoctorId());
                    dto.setName(user.getFirstName());
                    dto.setSurname(user.getLastName());
                    dto.setFullName(doctor.getTitle() + " " + user.getFirstName() + " " + user.getLastName());
                    dto.setDepartmentName(dept != null ? dept.getDepartmentName() : "Bölüm Belirtilmemiş");
                    dto.setTitle(doctor.getTitle());
                    dto.setSpecialization(doctor.getSpecialization());
                    dto.setExperienceYears(doctor.getExperienceYears());
                    dto.setIsAvailable(doctor.getIsAvailable());
                    dto.setIsActive(doctor.getIsActive());
                    dto.setAbout(doctor.getAbout());

                    return dto;
                })
                .toList();
    }

    public String deleteDoctor(UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_FOUND,"Doktor bulunamadı:" + id)));
        doctor.setIsActive(false);
        doctor.setIsAvailable(false);
        doctorRepository.save(doctor);
        return "Doktor pasif hale getirildi.";
    }
}
