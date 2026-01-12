package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCancel;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.enums.CancelledBy;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.model.*;
import com.dogukan.appointment.repository.AppointmentRepository;
import com.dogukan.appointment.repository.DepartmentRepository;
import com.dogukan.appointment.repository.DoctorRepository;
import com.dogukan.appointment.repository.DoctorScheduleRepository;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.service.IAppointmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorScheduleRepository scheduleRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  DoctorRepository doctorRepository,
                                  UserRepository userRepository,
                                  DepartmentRepository departmentRepository,
                                  DoctorScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    @Transactional
    public DtoAppointmentResponse createAppointment(DtoAppointmentCreate dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, dto.getUserId().toString())));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_FOUND, null)));
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DEPARTMENT_NOT_FOUND, null)));

        int dayOfWeek = dto.getAppointmentDate().getDayOfWeek().getValue();
        DoctorSchedule schedule = scheduleRepository
                .findByDoctor_DoctorIdAndDayOfWeekAndIsActiveTrue(doctor.getDoctorId(), dayOfWeek)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "Doktorun bu gün mesaisi bulunmuyor.")));

        LocalTime reqTime = dto.getAppointmentTime();

        if (reqTime.isBefore(schedule.getStartTime()) || reqTime.isAfter(schedule.getEndTime().minusMinutes(schedule.getAppointmentDuration()))) {
            throw new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "İstenen saat mesai dışındadır."));
        }

        if (schedule.getLunchBreakStart() != null && schedule.getLunchBreakEnd() != null) {
            if (!reqTime.isBefore(schedule.getLunchBreakStart()) && reqTime.isBefore(schedule.getLunchBreakEnd())) {
                throw new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "Doktor bu saatte öğle yemeğinde."));
            }
        }

        boolean isBusy = appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctor.getDoctorId(), dto.getAppointmentDate(), reqTime, AppointmentStatus.CANCELLED);

        if (isBusy) {
            throw new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "Bu randevu saati az önce doldu."));
        }

        Appointment appointment = Appointment.builder()
                .user(user)
                .doctor(doctor)
                .department(dept)
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(reqTime)
                .complaintText(dto.getComplaintText())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        DtoAppointmentResponse response = new DtoAppointmentResponse();
        BeanUtils.copyProperties(saved, response);
        response.setAppointmentStatus(saved.getStatus());
        response.setDoctorName(doctor.getDisplayName());
        response.setDepartmentName(dept.getDepartmentName());

        return response;
    }

    @Override
    @Transactional
    public DtoAppointmentResponse cancelAppointment(UUID id, DtoAppointmentCancel dto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, id.toString())));

        if (!appointment.canBeCancelled()) {
            throw new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_CANCELLABLE, "Randevuya çok az süre kalmış."));
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(dto.getCancelReason());
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelledBy(CancelledBy.PATIENT);

        return convertToResponseDto(appointmentRepository.save(appointment));
    }

    @Override
    public DtoAppointmentResponse getAppointmentById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, id.toString())));
        return convertToResponseDto(appointment);
    }

    @Override
    public List<DtoAppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DtoAppointmentResponse> getAllAppointmentsByUserId(UUID userId) {
        return appointmentRepository.findByUser_UserIdAndAppointmentDateGreaterThanEqual(userId, LocalDate.now().minusMonths(6))
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String deleteAppointment(UUID id) {
        if (!appointmentRepository.existsById(id)) {
            throw new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, id.toString()));
        }
        appointmentRepository.deleteById(id);
        return "Randevu başarıyla silindi.";
    }

    private DtoAppointmentResponse convertToResponseDto(Appointment appointment) {
        DtoAppointmentResponse response = new DtoAppointmentResponse();
        BeanUtils.copyProperties(appointment, response);
        if (appointment.getDoctor() != null) response.setDoctorName(appointment.getDoctor().getDisplayName());
        if (appointment.getDepartment() != null) response.setDepartmentName(appointment.getDepartment().getDepartmentName());
        return response;
    }
}
