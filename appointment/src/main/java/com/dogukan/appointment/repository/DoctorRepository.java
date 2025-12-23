package com.dogukan.appointment.repository;

import com.dogukan.appointment.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    /**
     * Belirtilen Bölüm ID'sine sahip doktorları listeler.
     * İlişki yolu: Doctor.department.departmentId
     *
     * @param departmentId Bölümün UUID'si.
     * @return İlgili bölüme ait doktorların listesi.
     */
    List<Doctor> findByDepartment_DepartmentId(UUID departmentId);
}
