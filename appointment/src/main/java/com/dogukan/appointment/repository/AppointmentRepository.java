package com.dogukan.appointment.repository;

import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByDoctor_DoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            UUID doctorId, LocalDate date, LocalTime time, AppointmentStatus status);

    List<Appointment> findByDoctor_DoctorIdAndAppointmentDate(UUID doctorId, LocalDate appointmentDate);

    List<Appointment> findByUser_UserIdAndAppointmentDateGreaterThanEqual(UUID userId, LocalDate date);
}
