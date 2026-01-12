package com.dogukan.appointment.repository;

import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a WHERE a.doctor.doctorId = :doctorId AND a.appointmentDate = :date AND a.appointmentTime = :time AND a.status NOT IN (com.dogukan.appointment.enums.AppointmentStatus.CANCELLED)")
    Optional<Appointment> findByDoctorAndDateTime(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date, @Param("time") LocalTime time);

    boolean existsByDoctor_DoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            UUID doctorId, LocalDate date, LocalTime time, AppointmentStatus status);



    List<Appointment> findByUserUserId(UUID id);

    /**
     * Belirli bir doktorun, belirli bir tarihteki tüm randevularını getirir.
     * Bu metot, müsaitlik (slot) hesaplaması için kritiktir.
     */
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDate(UUID doctorId, LocalDate appointmentDate);

    List<Appointment> findByUser_UserIdAndAppointmentDateGreaterThanEqual(UUID userId, LocalDate date);
}
