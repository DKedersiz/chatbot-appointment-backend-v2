package com.dogukan.appointment.service.impl;

import com.dogukan.appointment.enums.AppointmentStatus;
import com.dogukan.appointment.model.Appointment;
import com.dogukan.appointment.model.DoctorSchedule;
import com.dogukan.appointment.repository.AppointmentRepository;
import com.dogukan.appointment.repository.DoctorScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorScheduleService(DoctorScheduleRepository scheduleRepository,
                                     AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Senin DoctorSchedule modelindeki start/end/lunch bilgilerini kullanarak
     * gerçek boş randevu saatlerini üretir.
     */
    public List<LocalTime> findAvailableSlots(UUID doctorId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun

        DoctorSchedule schedule = scheduleRepository
                .findByDoctor_DoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek)
                .orElse(null);

        if (schedule == null) return new ArrayList<>();

        List<LocalTime> bookedTimes = appointmentRepository
                .findByDoctor_DoctorIdAndAppointmentDate(doctorId, date)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toList());

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime current = schedule.getStartTime();
        int duration = schedule.getAppointmentDuration();

        while (current.plusMinutes(duration).isBefore(schedule.getEndTime()) ||
                current.plusMinutes(duration).equals(schedule.getEndTime())) {

            boolean isLunch = false;
            if (schedule.getLunchBreakStart() != null && schedule.getLunchBreakEnd() != null) {
                if (!current.isBefore(schedule.getLunchBreakStart()) && current.isBefore(schedule.getLunchBreakEnd())) {
                    isLunch = true;
                }
            }

            if (!isLunch && !bookedTimes.contains(current)) {
                if (!date.equals(LocalDate.now()) || current.isAfter(LocalTime.now())) {
                    availableSlots.add(current);
                }
            }
            current = current.plusMinutes(duration);
        }

        return availableSlots;
    }
}
