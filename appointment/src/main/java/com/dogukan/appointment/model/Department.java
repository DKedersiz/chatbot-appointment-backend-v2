package com.dogukan.appointment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "departments",indexes = {
        @Index(name = "idx_department_name",columnList = "department_name"),
        @Index(name = "idx_department_active",columnList = "is_active")
})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "department_id",updatable = false,nullable = false)
    private UUID departmentId;

    @NotBlank(message = "Bölüm adı boş olamaz.")
    @Size(min = 3, max = 100, message = "Bölüm adı 3 ile 100 karakter arası olmalıdır.")
    @Column(name = "department_name",nullable = false,unique = true)
    private String departmentName;

    @Size(max = 1000,message = "Açıklama kısmı 1000 karakterden uzun olamaz.")
    @Column(name = "description",columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active",nullable = false)
    private boolean isActive = true;

    // REMOVE kasıtlı olarak kapsam dışı: bir bölümü silmek ona bağlı doktorları (ve dolayısıyla randevu geçmişlerini) silmemeli.
    @OneToMany(mappedBy = "department", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Doctor> doctors;

}
