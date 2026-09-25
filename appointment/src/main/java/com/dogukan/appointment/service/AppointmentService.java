package com.dogukan.appointment.service;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCancel;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.enums.CancelledBy;
import com.dogukan.appointment.enums.Role;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.model.*;
import com.dogukan.appointment.repository.AppointmentRepository;
import com.dogukan.appointment.repository.DoctorRepository;
import com.dogukan.appointment.repository.DoctorScheduleRepository;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.security.UserPrincipal;
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
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorScheduleRepository scheduleRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                                  DoctorRepository doctorRepository,
                                  UserRepository userRepository,
                                  DoctorScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional
    public DtoAppointmentResponse createAppointment(DtoAppointmentCreate dto, UserPrincipal currentUser) {
        assertOwnerOrAdmin(currentUser, dto.getUserId());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, dto.getUserId().toString())));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_FOUND, null)));
        if (!doctor.isActiveAndAvailable()) {
            throw new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "Doktor şu anda randevu kabul etmiyor."));
        }

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
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(reqTime)
                .complaintText(dto.getComplaintText())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        return convertToResponseDto(saved);
    }

    @Transactional
    public DtoAppointmentResponse cancelAppointment(UUID id, DtoAppointmentCancel dto, UserPrincipal currentUser) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, id.toString())));

        assertOwnerOrAdmin(currentUser, appointment.getUser().getUserId());

        if (!appointment.canBeCancelled()) {
            throw new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_CANCELLABLE, "Randevuya çok az süre kalmış."));
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(dto.getCancelReason());
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelledBy(CancelledBy.PATIENT);

        return convertToResponseDto(appointmentRepository.save(appointment));
    }

    public DtoAppointmentResponse getAppointmentById(UUID id, UserPrincipal currentUser) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, id.toString())));
        assertOwnerOrAdmin(currentUser, appointment.getUser().getUserId());
        return convertToResponseDto(appointment);
    }

    private void assertOwnerOrAdmin(UserPrincipal currentUser, UUID ownerUserId) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = currentUser.getUserId().equals(ownerUserId);
        if (!isAdmin && !isOwner) {
            throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED_REQUEST, "Bu randevuya erişim yetkiniz yok."));
        }
    }

    public List<DtoAppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<DtoAppointmentResponse> getAllAppointmentsByUserId(UUID userId) {
        return appointmentRepository.findByUser_UserIdAndAppointmentDateGreaterThanEqual(userId, LocalDate.now().minusMonths(6))
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

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
        response.setAppointmentStatus(appointment.getStatus());

        User patient = appointment.getUser();
        response.setUserId(patient.getUserId());
        response.setPatientName(patient.getFullName());
        response.setPatientEmail(patient.getEmail());
        response.setPatientPhone(patient.getPhoneNumber());

        Doctor doctor = appointment.getDoctor();
        response.setDoctorId(doctor.getDoctorId());
        response.setDoctorName(doctor.getDisplayName());
        response.setDoctorTitle(doctor.getTitle());
        response.setDoctorSpecialization(doctor.getSpecialization());

        Department department = appointment.getDepartment();
        if (department != null) {
            response.setDepartmentId(department.getDepartmentId());
            response.setDepartmentName(department.getDepartmentName());
        }
        return response;
    }
}
