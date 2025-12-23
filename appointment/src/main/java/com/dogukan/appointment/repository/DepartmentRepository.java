package com.dogukan.appointment.repository;

import com.dogukan.appointment.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /**
     * Bölüm adını (departmentName alanını) büyük/küçük harf duyarlılığı olmadan arar.
     * Bu, chatbot'tan gelen metinle eşleşen bölümü bulmak için kullanılır.
     *
     * @param departmentName Bölüm adı (tam veya kısmi).
     * @return Eşleşen Bölüm nesnesi (Optional içinde).
     */
    Optional<Department> findByDepartmentNameContainingIgnoreCase(String departmentName);
}
