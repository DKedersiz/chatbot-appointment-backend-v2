package com.dogukan.appointment.gemini;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.dto.appointment.DtoAppointmentResponse;
import com.dogukan.appointment.gemini.dto.ChatbotRequest;
import com.dogukan.appointment.gemini.dto.ChatbotResponse;
import com.dogukan.appointment.model.Department;
import com.dogukan.appointment.model.Doctor;
import com.dogukan.appointment.repository.DepartmentRepository;
import com.dogukan.appointment.repository.DoctorRepository;
import com.dogukan.appointment.service.IAppointmentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl {

    private final GeminiServiceImpl geminiService;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final IAppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    public ChatbotServiceImpl(GeminiServiceImpl geminiService, DepartmentRepository departmentRepository,
                              DoctorRepository doctorRepository, IAppointmentService appointmentService) {
        this.geminiService = geminiService;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentService = appointmentService;
        this.objectMapper = new ObjectMapper();
    }

    public ChatbotResponse processMessage(ChatbotRequest request) {
        // 1. Mevcut bölümleri string olarak al (departmentName alanını kullanıyoruz)
        String availableDepts = departmentRepository.findAll().stream()
                .map(Department::getDepartmentName)
                .collect(Collectors.joining(", "));

        // 2. Gemini'ye sor ve JSON al
        // KRİTİK DÜZELTME: history parametresini request objesinden aktarıyoruz.
        String aiJson = geminiService.extractIntentAndEntities(
                request.getQuery(),
                availableDepts,
                request.getHistory()
        );

        try {
            JsonNode aiData = objectMapper.readTree(aiJson);
            String intent = aiData.path("intent").asText();
            String missingInfo = aiData.path("missing_info").asText(null);

            // Eğer AI bilgi eksik dediyse veya anlayamadıysa direkt soruyu döndür
            if (missingInfo != null && !missingInfo.isEmpty() && !"null".equalsIgnoreCase(missingInfo)) {
                return new ChatbotResponse(missingInfo);
            }

            // 3. Verileri Çıkar
            String deptName = aiData.path("department").asText(null);
            String dateStr = aiData.path("date").asText(null);
            String timeStr = aiData.path("time").asText(null);

            // KRİTİK: Eğer AI bölümü çıkaramadıysa, kullanıcıya sor
            if (deptName == null || deptName.isEmpty() || "null".equalsIgnoreCase(deptName)) {
                return new ChatbotResponse("Randevu alabilmem için lütfen almak istediğiniz bölümü belirtin.");
            }

            // 4. Bölüm Kontrolü
            Optional<Department> department = departmentRepository.findByDepartmentNameContainingIgnoreCase(deptName);
            if (department.isEmpty()) {
                return new ChatbotResponse("Üzgünüm, '" + deptName + "' adında bir bölümümüz bulunmamaktadır.");
            }

            // 5. Tarih Kontrolü
            LocalDate date = LocalDate.parse(dateStr);

            // 6. Doktor Bul
            List<Doctor> doctors = doctorRepository.findByDepartment_DepartmentId(department.get().getDepartmentId());
            if (doctors.isEmpty()) {
                return new ChatbotResponse("Bu bölümde şu an müsait doktor bulunmamaktadır.");
            }
            Doctor doctor = doctors.get(0);

            // 7. Randevu Oluşturma veya Sorgulama
            if (timeStr != null && !"null".equalsIgnoreCase(timeStr)) {
                LocalTime time = LocalTime.parse(timeStr);

                // Randevu oluşturmayı dene
                try {
                    DtoAppointmentCreate createRequest = new DtoAppointmentCreate();
                    createRequest.setUserId(UUID.fromString(request.getUserId()));
                    createRequest.setDoctorId(doctor.getDoctorId());
                    createRequest.setDepartmentId(department.get().getDepartmentId());
                    createRequest.setAppointmentDate(date);
                    createRequest.setAppointmentTime(time);
                    createRequest.setComplaintText("Chatbot üzerinden oluşturuldu.");

                    DtoAppointmentResponse response = appointmentService.createAppointment(createRequest);

                    return new ChatbotResponse("Harika! " + department.get().getDepartmentName() + " bölümü, " +
                            doctor.getDisplayName() + " için " +
                            date + " saat " + time + "'a randevunuz oluşturuldu.");

                } catch (Exception e) {
                    return new ChatbotResponse("Randevu oluştururken sorun oldu: " + e.getMessage());
                }
            } else {
                // Saat belirtilmemişse müsait saatleri öner
                return new ChatbotResponse(department.get().getDepartmentName() + " için " + date + " tarihinde randevu bakıyorum. " +
                        "Saat 09:00, 10:00 veya 14:00 uygun mu? Lütfen bir saat belirtin.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatbotResponse("İsteğinizi işlerken teknik bir hata oluştu veya AI veriyi doğru çözümleyemedi. Hata: " + e.getMessage());
        }
    }
}