package com.dogukan.appointment.gemini;

import com.dogukan.appointment.dto.appointment.DtoAppointmentCreate;
import com.dogukan.appointment.gemini.dto.ChatbotRequest;
import com.dogukan.appointment.gemini.dto.ChatbotResponse;
import com.dogukan.appointment.gemini.dto.MessageHistory;
import com.dogukan.appointment.model.Department;
import com.dogukan.appointment.model.Doctor;
import com.dogukan.appointment.repository.DepartmentRepository;
import com.dogukan.appointment.repository.DoctorRepository;
import com.dogukan.appointment.service.IAppointmentService;
import com.dogukan.appointment.service.impl.DoctorScheduleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl {

    private final GeminiServiceImpl geminiService;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final IAppointmentService appointmentService;
    private final DoctorScheduleService scheduleService;
    private final ObjectMapper objectMapper;

    public ChatbotServiceImpl(GeminiServiceImpl geminiService, DepartmentRepository departmentRepository,
                              DoctorRepository doctorRepository, IAppointmentService appointmentService,
                              DoctorScheduleService scheduleService) {
        this.geminiService = geminiService;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentService = appointmentService;
        this.scheduleService = scheduleService;
        this.objectMapper = new ObjectMapper();
    }

    public ChatbotResponse processMessage(ChatbotRequest request) {
        try {
            LocalDate today = LocalDate.now();
            String dateContext = String.format("Bugün: %s (%s).", today, today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("tr")));

            String hospitalCatalog = getHospitalCatalog();
            String slotsInfo = getRealSlotsContext(request.getQuery(), request.getHistory());

            String systemPrompt = dateContext + "\n" +
                    "Sen MediRandevu Hastanesi'nin TAM YETKİLİ asistanısın. Kullanıcı deneyimini kusursuz tutmalısın.\n" +
                    "### KURALLAR:\n" +
                    "1. Kullanıcı 'en yakın saat', 'fark etmez', 'en uygunu olsun' derse; MÜSAİTLİKLER listesindeki İLK boş saati ve doktoru kendin SEÇ ve intent alanını RANDEVU_OLUSTUR yap.\n" +
                    "2. 'Yapıştır', 'olur', 'kuzen al', 'onaylıyorum', 'tamamdır' gibi ifadelerin tamamı ONAYDIR.\n" +
                    "3. Eğer kullanıcı tarih belirtmezse SADECE son mesajdaki tarihi, o da yoksa bugünü baz al.\n" +
                    "4. doctor_name alanına asla ünvan koyma (Sadece 'Selin Beyin' gibi).\n" +
                    "5. Yanıtını daima 'chat_response' alanına yaz.\n" +
                    "### VERİ KATALOĞU:\n" + hospitalCatalog + "\n" +
                    "### MÜSAİTLİKLER (Gerçek Zamanlı):\n" + slotsInfo;

            System.out.println(hospitalCatalog);

            String aiJson = geminiService.extractIntentAndEntities(request.getQuery(), systemPrompt,
                    "Kullanıcı belirsiz konuştuysa (fark etmez vb.) insiyatif al ve en yakın saati seçerek randevuyu oluştur.", request.getHistory());

            String cleanJson = aiJson.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode data = objectMapper.readTree(cleanJson);

            String intent = data.path("intent").asText("SOHBET");

            if ("RANDEVU_OLUSTUR".equalsIgnoreCase(intent)) {
                return handleCreateFinal(data, request.getUserId(), request.getHistory(), systemPrompt);
            }

            return new ChatbotResponse(data.path("chat_response").asText("Size nasıl yardımcı olabilirim?"));

        } catch (Exception e) {
            return new ChatbotResponse("Şu an talebinizi işleyemiyorum, lütfen tekrar dener misiniz?");
        }
    }

    private ChatbotResponse handleCreateFinal(JsonNode data, String userId, List<MessageHistory> history, String systemPrompt) {
        try {
            if (userId == null || userId.isEmpty() || userId.equals("null")) {
                return new ChatbotResponse("Oturumunuz kapalı. Lütfen giriş yapın.");
            }

            String departmentName = data.path("department").asText();
            String doctorNameFromAi = data.path("doctor_name").asText();
            String dateStr = data.path("date").asText();
            String timeStr = data.path("time").asText();

            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                throw new RuntimeException("Tarih veya saat belirlenemedi.");
            }

            Department dept = departmentRepository.findByDepartmentNameContainingIgnoreCase(departmentName)
                    .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı: " + departmentName));

            List<Doctor> doctors = doctorRepository.findByDepartment_DepartmentId(dept.getDepartmentId());
            Doctor selectedDoc = doctors.stream()
                    .filter(d -> {
                        String dbName = d.getDisplayName().toLowerCase(new Locale("tr")).replaceAll("prof\\.|dr\\.|doç\\.|uzm\\.", "").trim();
                        String aiName = doctorNameFromAi.toLowerCase(new Locale("tr")).replaceAll("prof\\.|dr\\.|doç\\.|uzm\\.", "").trim();
                        return dbName.contains(aiName) || aiName.contains(dbName);
                    }).findFirst().orElse(doctors.get(0));

            appointmentService.createAppointment(DtoAppointmentCreate.builder()
                    .userId(UUID.fromString(userId))
                    .departmentId(dept.getDepartmentId())
                    .doctorId(selectedDoc.getDoctorId())
                    .appointmentDate(LocalDate.parse(dateStr))
                    .appointmentTime(LocalTime.parse(timeStr))
                    .complaintText(data.path("complaint").asText("Akıllı Asistan Kaydı"))
                    .build());

            return new ChatbotResponse(data.path("chat_response").asText("Randevunuz başarıyla oluşturuldu."));

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            String recoveryPrompt = "SİSTEM HATASI: Randevu oluşturulamadı. Sebep: " + errorMsg +
                    "\nLütfen kullanıcıya durumu açıkla ve mevcut MÜSAİTLİKLER listesinden başka bir alternatif sun.";
            try {
                String aiRetryJson = geminiService.extractIntentAndEntities(recoveryPrompt, systemPrompt, "Kullanıcıyı üzmeden alternatif üret.", history);
                JsonNode retryData = objectMapper.readTree(aiRetryJson.replaceAll("```json", "").replaceAll("```", "").trim());
                return new ChatbotResponse(retryData.path("chat_response").asText("Üzgünüm, bir sorun oluştu. Başka bir saat seçmek ister misiniz?"));
            } catch (Exception inner) {
                return new ChatbotResponse("Seçilen saat uygun değil, lütfen listeden başka bir saat seçin.");
            }
        }
    }

    private String getHospitalCatalog() {
        return departmentRepository.findAll().stream()
                .map(d -> "- " + d.getDepartmentName() + ": " +
                        doctorRepository.findByDepartment_DepartmentId(d.getDepartmentId())
                                .stream().map(Doctor::getDisplayName).collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n"));
    }

    private String getRealSlotsContext(String query, List<MessageHistory> history) {
        String currentMsg = query.toLowerCase(new Locale("tr"));
        String historyStr = history.stream().map(MessageHistory::getText).collect(Collectors.joining(" ")).toLowerCase(new Locale("tr"));

        Department detectedDept = departmentRepository.findAll().stream()
                .filter(d -> currentMsg.contains(d.getDepartmentName().toLowerCase(new Locale("tr"))) ||
                        historyStr.contains(d.getDepartmentName().toLowerCase(new Locale("tr"))) ||
                        currentMsg.contains("çocuk") || historyStr.contains("çocuk"))
                .findFirst().orElse(null);

        if (detectedDept == null) return "Henüz branş seçilmedi.";

        LocalDate targetDate = parseTargetDateSmart(currentMsg, historyStr);

        if (targetDate.getDayOfWeek() == DayOfWeek.SATURDAY || targetDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return targetDate + " haftasonu, hastane kapalı. Lütfen hafta içi öner.";
        }

        StringBuilder sb = new StringBuilder(targetDate + " tarihli müsaitlikler:\n");
        List<Doctor> docs = doctorRepository.findByDepartment_DepartmentId(detectedDept.getDepartmentId());

        boolean anySlot = false;
        for (Doctor doc : docs) {
            List<LocalTime> slots = scheduleService.findAvailableSlots(doc.getDoctorId(), targetDate);
            if (!slots.isEmpty()) {
                sb.append("- Dr. ").append(doc.getDisplayName()).append(": ").append(slots).append("\n");
                anySlot = true;
            }
        }
        return anySlot ? sb.toString() : "O gün için hiç boş randevu yok.";
    }

    private LocalDate parseTargetDateSmart(String currentMsg, String history) {
        LocalDate now = LocalDate.now();
        String text = (currentMsg + " " + history).toLowerCase(new Locale("tr"));


        if (currentMsg.contains("bugün")) return now;
        if (currentMsg.contains("yarın")) return now.plusDays(1);

        if (text.contains("pazartesi")) return getNextDay(now, DayOfWeek.MONDAY);
        if (text.contains("salı")) return getNextDay(now, DayOfWeek.TUESDAY);
        if (text.contains("çarşamba")) return getNextDay(now, DayOfWeek.WEDNESDAY);
        if (text.contains("perşembe")) return getNextDay(now, DayOfWeek.THURSDAY);
        if (text.contains("cuma")) return getNextDay(now, DayOfWeek.FRIDAY);

        Pattern p = Pattern.compile("(\\d{1,2})\\s*(ocak|şubat|mart|nisan|mayıs|haziran|temmuz|ağustos|eylül|ekim|kasım|aralık)");
        Matcher m = p.matcher(text);
        if (m.find()) return LocalDate.of(2026, getMonth(m.group(2)), Integer.parseInt(m.group(1)));

        return now;
    }

    private LocalDate getNextDay(LocalDate from, DayOfWeek day) {
        LocalDate d = from;
        if(d.getDayOfWeek() == day) d = d.plusDays(1);
        while (d.getDayOfWeek() != day) d = d.plusDays(1);
        return d;
    }

    private int getMonth(String m) {
        return switch (m.toLowerCase()) { case "ocak" -> 1; case "şubat" -> 2; case "mart" -> 3; case "nisan" -> 4; case "mayıs" -> 5; case "haziran" -> 6; default -> 1; };
    }
}