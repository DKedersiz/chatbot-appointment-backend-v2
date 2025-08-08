package com.dogukan.appointment.repository;

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

    @Query("SELECT a FROM Appointment a WHERE a.doctor.doctorId = :doctorId AND a.appointmentDate = :date AND a.appointmentTime = :time AND a.status NOT IN ('CANCELLED')")
    Optional<Appointment> findByDoctorAndDateTime(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date, @Param("time") LocalTime time);


    List<Appointment> findByUserUserId(UUID id);
}
