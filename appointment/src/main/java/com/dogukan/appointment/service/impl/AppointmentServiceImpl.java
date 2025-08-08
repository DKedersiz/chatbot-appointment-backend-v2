package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCancel;
import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.ErrorMessage;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.mapper.AppointmentMapper;
import com.dogukan.appointment.mapper.DoctorMapper;
import com.dogukan.appointment.model.Appointment;
import com.dogukan.appointment.model.DoctorSchedule;
import com.dogukan.appointment.repository.AppointmentRepository;
import com.dogukan.appointment.repository.DepartmentRepository;
import com.dogukan.appointment.repository.DoctorRepository;
import com.dogukan.appointment.repository.DoctorScheduleRepository;
import com.dogukan.appointment.repository.UserRepository;
import com.dogukan.appointment.service.IAppointmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentServiceImpl implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, UserRepository userRepository,
                                  DoctorRepository doctorRepository, DepartmentRepository departmentRepository,
                                  DoctorScheduleRepository doctorScheduleRepository, DoctorMapper doctorMapper,
                                  AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public DtoAppointmentResponse createAppointment(DtoAppointmentCreate appointmentCreate) {
        var user = userRepository.findById(appointmentCreate.getUserId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Kullanıcı bulunamadı: " + appointmentCreate.getUserId())));
        var doctor = doctorRepository.findById(appointmentCreate.getDoctorId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_FOUND, "Doktor bulunamadı: " + appointmentCreate.getDoctorId())));
        var department = departmentRepository.findById(appointmentCreate.getDepartmentId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DEPARTMENT_NOT_FOUND, "Bölüm bulunamadı: " + appointmentCreate.getDepartmentId())));

        if (!doctor.isActiveAndAvailable()) {
            throw new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "Doktor müsait değil: " + doctor.getDisplayName()));
        }

        int dayOfWeek = appointmentCreate.getAppointmentDate().getDayOfWeek().getValue();
        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorDoctorIdAndDayOfWeek(doctor.getDoctorId(), dayOfWeek)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "Doktor bu günde müsait değil.")));
        if (!appointmentCreate.getAppointmentTime().isAfter(schedule.getStartTime()) ||
                !appointmentCreate.getAppointmentTime().isBefore(schedule.getEndTime())) {
            throw new BaseException(new ErrorMessage(MessageType.DOCTOR_NOT_AVAILABLE, "Randevu saati doktorun çalışma saatleri dışında."));
        }

        if (appointmentRepository.findByDoctorAndDateTime(doctor.getDoctorId(), appointmentCreate.getAppointmentDate(), appointmentCreate.getAppointmentTime()).isPresent()) {
            throw new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_CANCELLABLE, "Bu saatte başka bir randevu mevcut."));
        }

        Appointment appointment = appointmentMapper.toAppointment(appointmentCreate);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDtoAppointmentResponse(savedAppointment);
    }

    @Override
    public DtoAppointmentResponse cancelAppointment(UUID appointmentId, DtoAppointmentCancel appointmentCancel) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, "Randevu bulunamadı: " + appointmentId)));
        if (!appointment.canBeCancelled()) {
            throw new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_CANCELLABLE, "Bu randevu iptal edilemez: " + appointmentId));
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment.setCancelReason(appointmentCancel.getCancelReason());
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelledBy(appointmentCancel.getCancelledBy());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDtoAppointmentResponse(savedAppointment);
    }

    @Override
    public DtoAppointmentResponse getAppointmentById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, "Randevu bulunamadı: " + id)));
        return appointmentMapper.toDtoAppointmentResponse(appointment);
    }

    @Override
    public List<DtoAppointmentResponse> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointmentMapper.toDtoAppointmentResponseList(appointments);
    }

    @Override
    public List<DtoAppointmentResponse> getAllAppointmentsByUserId(UUID id) {
        List<Appointment> appointments = appointmentRepository.findByUserUserId(id);
        return appointmentMapper.toDtoAppointmentResponseList(appointments);
    }

    @Override
    public String deleteAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.APPOINTMENT_NOT_FOUND, "Randevu bulunamadı: " + id)));
        appointmentRepository.delete(appointment);
        return "Randevu silindi: " + id;
    }
}
