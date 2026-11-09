# Mimari Notlar ve Çıkarılan Dersler

Bu belge, hastane randevu sistemi backend'inin (Spring Boot + PostgreSQL + Gemini) gözden geçirilmesi sırasında bulunan tasarım hatalarını, bunların neden hatalı olduğunu ve benzer bir projede nasıl düşünülmesi gerektiğini özetler. Değişikliklerin kendisi `f2f6012` commit'inde yer alır.

Belgede tekrar eden bir tema var: **aynı gerçeği birden fazla yerde, birbirinden habersiz tanımlamak.** Bulguların çoğu bu hatanın farklı biçimleridir.

---

## 1. Güvenlik

### 1.1 Secret'lar git geçmişine girmişti
- **Ne oldu:** JWT secret'ı, Gemini API key'i ve Gmail app password'ü `application.properties` dosyalarında düz metin olarak commit'lenmişti. Sonradan değerler placeholder'larla değiştirildi, ama eski commit'lerde hâlâ duruyorlar ve repo public.
- **Ders:** Bir secret'ı sonraki commit'te silmek onu yok etmez, sadece güncel hâlden kaldırır. Sızmış bir secret'ın tek gerçek çözümü onu **iptal edip yenilemektir**.
- **Doğrusu:** Secret'lar baştan ortam değişkenlerinden (`${JWT_SECRET_KEY}`) ya da git'e girmeyen bir dosyadan (`application-local.properties` + `.gitignore`) okunmalı. Test ortamı gerçek secret'lara hiç ihtiyaç duymamalı.

### 1.2 Token saklama stratejisine karar verilmemişti
- **Ne oldu:** Token'lar hem httpOnly cookie olarak set ediliyor hem de JSON body'de dönülüyordu. Filter ise hem cookie'yi hem `Authorization` header'ını kabul ediyordu. Önce header ile başlanmış, sonra "güvenli olsun" diye cookie eklenmiş, ama eski yol temizlenmemiş.
- **Neden yanlış:** httpOnly cookie'nin tek amacı token'ı JavaScript'ten saklamaktır. Aynı token body'de de dönüyorsa bu koruma sıfırlanır. "Hibrit" yaklaşım iki seçeneğin avantajlarını değil, dezavantajlarını birleştirir.
- **Doğrusu:** Frontend mimarisine göre tek bir yol seçilmeli:
  - Aynı ekosistemde bir web uygulaması → httpOnly cookie + `SameSite` / CSRF koruması
  - Mobil uygulama ya da farklı domain'deki bir client → `Authorization` header
- **Ek hata:** Refresh cookie'nin `path` değeri gerçek endpoint'le eşleşmiyordu (`/api/auth/refresh-token` ile `/rest/api/auth/refreshToken`). Tarayıcı bu cookie'yi refresh endpoint'ine hiç göndermiyordu.

### 1.3 Yetkilendirme katmanı hiç çalışmıyordu
Burada birbirini maskeleyen iki hata vardı:
1. `@EnableMethodSecurity` eklenmemişti. Bu yüzden projedeki **bütün** `@PreAuthorize` annotation'ları hiç değerlendirilmiyordu. Giriş yapmış herhangi bir kullanıcı admin endpoint'lerini çağırabiliyordu.
2. Yetkiler `ROLE_` öneki olmadan veriliyordu (`"ADMIN"`). `hasRole('ADMIN')` ise `ROLE_ADMIN` arar. Metot güvenliği açıldığı anda bu kez hiç kimse hiçbir rol kontrolünü geçemedi.

- **Ders:** Güvenlik annotation'larının kodda **bulunması**, çalıştıkları anlamına gelmez. Bir güvenlik mekanizması eklendiğinde ya da açıldığında, "izin verilmesi gereken kişi geçiyor mu, reddedilmesi gereken kişi reddediliyor mu" sorusu uçtan uca test edilmeli. Endpoint'leri çağıran birkaç `MockMvc` testi bu iki hatayı da anında yakalardı.

### 1.4 Rol kontrolü yetmez, sahiplik kontrolü de gerekir (IDOR)
- **Ne oldu:** Randevu görüntüleme, iptal etme ve oluşturma endpoint'leri sadece "USER mısın" diye bakıyordu, "bu kayıt senin mi" diye bakmıyordu. Herhangi bir hasta, başka bir hastanın randevusunu (şikayet metni dahil) görebilir ya da iptal edebilirdi.
- **Doğrusu:** Path'te doğrudan `userId` varsa SpEL yeterli olur (`#id == principal.userId`). Kaydın sahibi ancak veritabanından okununca belli oluyorsa, kontrol **servis katmanında**, kayıt yüklendikten sonra yapılmalı (`assertOwnerOrAdmin`).

### 1.5 Mass assignment: kullanıcı kendini admin yapabiliyordu
- **Ne oldu:** Profil güncelleme DTO'su `role` alanını içeriyordu ve mapper DTO'daki bütün alanları entity'ye kopyalıyordu. Kullanıcı isteğin body'sine `"role": "ADMIN"` ekleyerek kendi yetkisini yükseltebiliyordu.
- **Doğrusu:** Güncellenebilir alanlar açıkça listelenmeli. Hassas alanlar (rol, durum, doğrulama bilgisi) ya DTO'da hiç olmamalı ya da mapper'da `ignore` edilip ayrıca yetki kontrolüyle ele alınmalı. "Kullanıcı ve admin aynı DTO'yu kullanıyor" durumu bu açığın klasik kaynağıdır.

### 1.6 Kimlik client'tan alınmamalı
- **Ne oldu:** Randevu oluşturma ve chatbot istekleri `userId` değerini request body'den alıyordu.
- **Doğrusu:** Kimlik zaten `SecurityContext` içinde mevcut. Client'ın gönderdiği kimlik bilgisine güvenmek, gereksiz bir saldırı yüzeyi açar.

### 1.7 Yarım kalmış güvenlik özellikleri
- **Hesap kilitleme:** `failedLoginAttempts` ve `lockedUntil` alanları ile kilit kontrolü vardı, ama sayacı artıran kod yoktu. Hesap hiçbir zaman kilitlenmiyordu.
- **JWT filter:** Token imzasını doğruluyordu, ama hesabın pasif ya da kilitli olup olmadığına bakmıyordu. Pasife alınan bir kullanıcının token'ı süresi dolana kadar çalışmaya devam ediyordu. Kullanıcı zaten her istekte veritabanından yükleniyordu, eksik olan sadece o bilginin kullanılmasıydı.
- **Ders:** Var olan ama bağlanmamış bir güvenlik özelliği, hiç olmayan bir özellikten daha tehlikelidir, çünkü koruma varmış gibi bir izlenim yaratır.

### 1.8 Aynı kural iki yerde
- **Ne oldu:** Hangi path'lerin herkese açık olduğu hem `SecurityConfig` içinde hem de `JwtAuthFilter` içinde ayrı listelerle tanımlanmıştı ve listeler farklıydı. `permitAll` kuralı, controller yeniden adlandırıldıktan sonra artık var olmayan bir path'i (`/user/**`) hedefliyordu.
- **Ders:** Güvenlik kuralının tek bir kaynağı olmalı. Ayrıca bir "typo'yu düzeltmeden" önce o typo'nun kazara ne işe yaradığı anlaşılmalı. `/user/**` kuralını `/users/**` olarak düzeltmek, o endpoint'leri gerçekten herkese açmak anlamına gelirdi.

---

## 2. Katmanlama ve soyutlama

### 2.1 Her sınıfın önüne interface koymak
- **Ne oldu:** 4 controller ve 7 servisin hepsi `IXxx` + `XxxImpl` çifti olarak yazılmıştı. Hiçbirinin ikinci bir implementasyonu yoktu. `logout()` metodu interface'e hiç eklenmemişti, yani iki dosyayı senkron tutma disiplini bile korunamamıştı.
- **Yanlış inançlar:**
  - *"DI için interface gerekir."* Gerekmez. Spring concrete class'ları da aynı şekilde inject eder.
  - *"Mock'lamak için interface gerekir."* Gerekmez. Mockito concrete class'ları da mock'layabilir.
  - *"`@Transactional` için interface gerekir."* Artık gerekmez. Spring Boot varsayılan olarak CGLIB class proxy'si kullanır.
- **Ne zaman gerçekten gerekir:** Birden fazla implementasyon gerçekten varsa ya da planlanıyorsa (örneğin `@Profile` ile gerçek SMTP ve sahte e-posta servisi), bir modül ya da takım sınırında sözleşme gerekiyorsa, ya da framework bunu zorunlu kılıyorsa (Spring Data repository'leri).
- **İlke:** DIP (Dependency Inversion Principle) "her sınıfa interface yaz" demez. "Kararlı soyutlamalara bağımlı ol" der. Tek sorumluluğu olan bir sınıfın public API'si çoğu zaman zaten yeterli bir soyutlamadır. Soyutlama ihtiyaç doğduğunda eklenmeli (YAGNI).

### 2.2 Framework sözleşmesi ile domain modelini karıştırmak
- **Ne oldu:** `User` JPA entity'si doğrudan `UserDetails`'i implement ediyordu. Security context'te canlı bir Hibernate entity'si taşınıyordu. İlgisiz bir kod (`JwtService`), sadece bu arayüz yüzünden var olan `getUsername()` metoduna bağımlı hale gelmişti.
- **Doğrusu:** Security'nin gördüğü nesne domain modelinden daha dar olmalı. Ayrı bir `UserPrincipal` sınıfı, login anındaki bilgilerin değişmez bir kopyasını taşımalı. Bu, Adapter deseninin doğrudan bir uygulamasıdır.

### 2.3 Ölü ve yarım kalmış kod
Projede hiç kullanılmayan pek çok parça vardı:
- Tüm sohbet kayıt altyapısı (`ChatSession`, `ChatMessage`, servis, repository, enum'lar). Doğru tasarım düşünülmüş, sonra yarım bırakılmış.
- Hiçbir yerde çağrılmayan mapper'lar ve repository metotları.
- Kimsenin kullanmadığı bir `RestBaseController` ve enum yardımcı metotları.
- Kullanılmayan bağımlılıklar (`google-genai` SDK'sı eklenmiş, ama çağrılar elle `RestTemplate` ile yapılıyor).
- **Ders:** Kullanılmayan kod nötr değildir. Okuyanı yanıltır ("bu özellik var sanıyordum"), bakım yükü getirir ve hatalarını saklar.

---

## 3. Veri modeli

### 3.1 Türetilebilir veriyi ayrıca saklamak
- **Ne oldu:** `Appointment` hem `doctor` hem `department` foreign key'ini tutuyordu. Doktor zaten bir bölüme bağlıydı ve iki bilginin tutarlı olduğunu kontrol eden bir kod yoktu. Kardiyoloji bölümü seçilip Dahiliye doktoruyla randevu oluşturulabiliyordu.
- **Doğrusu:** Bir veri başka bir veriden türetilebiliyorsa saklanmamalı, hesaplanmalı (`appointment.getDoctor().getDepartment()`). Böylece tutarsızlık yapısal olarak imkânsız hale gelir. Performans için kasıtlı bir denormalizasyon yapılıyorsa, tutarlılığı garanti eden bir mekanizma (constraint, trigger, servis kontrolü) şarttır.

### 3.2 Validasyon kuralları iki yerde
- **Ne oldu:** Aynı kurallar hem DTO'da hem entity'de yazılmıştı ve birbirinden ayrılmıştı. Örneğin `specialization` alanı DTO'da en fazla 100, entity'de en fazla 150 karakter olabiliyordu.
- **Ders:** Bir kuralı değiştirmek için birden fazla dosyayı değiştirmek gerekiyorsa, o kural yanlış yerde yaşıyor demektir.

### 3.3 Silme stratejisi kendi içinde çelişkiliydi
- **Ne oldu:** Entity'lerde soft-delete için `isActive` alanı vardı, ama silme işlemleri gerçek `DELETE` sorgusuyla yapılıyordu. Üstelik `Department → Doctor` ilişkisinde `CascadeType.ALL` vardı, yani bir bölüm silinirse bütün doktorları da siliniyordu.
- **Sağlık verisine özel not:** Bu tür sistemlerde "silme" genellikle hard delete olmaz. Tıbbi kayıtların yasal saklama zorunlulukları vardır, KVKK/GDPR'daki "unutulma hakkı" ile bu zorunluluklar çatışabilir. Endüstrideki yaygın model soft-delete ve gerektiğinde kişisel verilerin anonimleştirilmesidir. Gerçek bir sistemde bu karar hukuk danışmanıyla verilmelidir.
- **Ders (burada yaptığım bir hata):** Soft-delete'e geçildiğinde, veriyi **okuyan** her yolun da güncellenmesi gerekir. Bu adım atlandığı için pasife alınmış doktorlara hâlâ randevu alınabiliyordu. Bir silme stratejisini değiştirdiğinde "bu entity'yi kim, nerede okuyor" sorusu sorulmalı.
- **`CascadeType.ALL` hakkında:** Bu değer `REMOVE`'u da kapsar. Anlamlı ve bağımsız entity'ler (doktor, randevu) arasında neredeyse hiçbir zaman doğru seçim değildir.

### 3.4 `ddl-auto=update` hiçbir şeyi silmez
Hibernate bu modda yeni tablo ve kolon ekler, ama var olanları asla silmez. Entity'den bir alan kaldırıldığında veritabanında kullanılmayan kolonlar birikir. Ciddi bir projede şema değişiklikleri Flyway ya da Liquibase gibi bir migration aracıyla yönetilmelidir.

---

## 4. API sözleşmesi

### 4.1 Tutarlı bir response şekli
- **Ne oldu:** Başarılı cevaplar `RootEntity` (`result`, `errorMessage`, `data`), iş hataları `ApiError` şeklinde dönüyordu. Validasyon hataları ise hiç yakalanmıyor, Spring'in kendi ham JSON'u olarak dönüyordu. Yani üç farklı şekil vardı.
- **Doğrusu:** Başarı ve hata aynı zarfı kullanmalı. HTTP status kodu ise zarftan bağımsız olarak doğru anlamı taşımalı.

### 4.2 HTTP status kodları anlamlı olmalı
- **Ne oldu:** Bütün iş hataları `400` dönüyordu. Kök sebep, `BaseException`'ın hangi hata tipinden oluştuğunu saklamamasıydı. Sadece düzleştirilmiş bir mesaj metni tutuyordu, bu yüzden handler hataları birbirinden ayırt edemiyordu.
- **Doğrusu:** Exception, hata tipini (`MessageType`) taşımalı. Handler bu tipi status koduna çevirmeli: 404 bulunamadı, 409 çakışma, 401 kimlik doğrulama, 403 yetki. Bu eşleme `MessageType` içine değil handler'a konulmalı, böylece domain katmanı HTTP'yi bilmek zorunda kalmaz.

### 4.3 Genel `catch (Exception e)` handler'ları dikkatli yazılmalı
- **Ders (burada yaptığım bir hata):** Her şeyi yakalayan bir handler, Spring Security'nin kendi exception'larını da yutar. Hatalı şifre ve yetki reddi durumları 500 dönmeye başlamıştı. Genel handler eklenirken `AuthenticationException` (401) ve `AccessDeniedException` (403) için ayrı handler'lar da yazılmalı.

### 4.4 DTO alanlarının gerçekten dolduğundan emin olmak
- **Ne oldu:** `DtoAppointmentResponse` içindeki durum, hasta ve ID alanları hiçbir yerde set edilmiyordu, her cevapta `null` dönüyorlardı. Doktor DTO'sundaki `userId` ve `departmentId` de aynı durumdaydı.
- **Sebep:** `BeanUtils.copyProperties` sadece aynı isimdeki alanları kopyalar (entity'de `status`, DTO'da `appointmentStatus`). MapStruct da iç içe alanları (`user.userId`) açık bir `@Mapping` olmadan eşleyemez.
- **Ek hata:** `isToday()` bir `LocalDate` değerini `LocalDateTime` ile karşılaştırıyordu, bu yüzden her zaman `false` dönüyordu. Jackson bu metodu `today` alanı olarak JSON'a da yazıyordu.
- **Ders:** Getter metotları (`getX`, `isX`) Jackson tarafından JSON'a yazılır. DTO'ya eklenen her "yardımcı metot" aslında API sözleşmesinin bir parçası olur.

---

## 5. LLM (chatbot) entegrasyonu

### 5.1 Asıl sorun: sorumluluk sınırlarının çizilmemesi
Chatbot "dev bir system prompt + JSON şema + elle yazılmış regex" üzerine kuruluydu. Aynı işi (tarih hesaplama) hem model hem Java kodu ayrı ayrı yapmaya çalışıyordu. İş kuralları (hafta sonu yasağı, "en yakın saati seç") hem kodda hem prompt içinde doğal dille tekrar tanımlanmıştı.

Model ile kod arasındaki doğru sınır:

| Sorumluluk | Kim yapmalı |
|---|---|
| Kullanıcının niyetini anlamak | Model |
| Serbest metinden bölüm, doktor, tarih **ifadesini** çıkarmak | Model |
| Bu ifadeyi gerçek bir veritabanı kaydına eşlemek | Kod |
| Tarih ve saati normalize edip doğrulamak | Kod |
| "En yakın boş saat" gibi algoritmik kararlar | Kod |
| İş kurallarını uygulamak (mesai, çakışma, sahiplik) | Kod (mevcut servis) |
| Kullanıcıya doğal dilde cevap vermek | Model |

Projenin doğru yaptığı bir şey vardı: chatbot randevuyu, diğer bütün client'lar gibi `AppointmentService` üzerinden oluşturuyordu. İş kuralları bu yüzden tek bir yerde uygulanıyordu.

### 5.2 Bugün olsa nasıl tasarlanırdı: tool calling
Modele `list_departments`, `find_doctors`, `find_available_slots`, `propose_appointment` ve `confirm_appointment` gibi küçük araçlar verilir. Model ihtiyaç duyduğu veriyi bu araçlarla ister. Backend her aracı deterministik olarak çalıştırır ve doğrular.

- **Kimlik asla araç parametresi olmaz.** Backend, aracı çalıştırırken `SecurityContext`'teki kullanıcıyı kullanır.
- **Yazma işlemleri iki adımlıdır.** Önce öneri, sonra onay. Onay modelin yorumuna bırakılmaz ("'olur' da onaydır" gibi bir kural yazılmaz), kodda zorunlu bir adımdır.
- **Konuşma geçmişi sunucuda tutulur.** Client'tan gelen geçmiş değiştirilebilir ve modele sahte bir "asistan" mesajı enjekte edilebilir.
- **System prompt küçük tutulur:** rol, ton, bugünün tarihi ve birkaç davranış kuralı. İş kuralları prompt'a girmez.

### 5.3 Maliyet ve verimlilik
Her mesajda bütün hastane kataloğu, müsaitlik listesi ve konuşmanın tamamı modele gönderiliyordu. Kolaydan zora doğru iyileştirmeler:
1. Konuşma geçmişini son N mesajla sınırlamak (uygulandı).
2. Nadiren değişen verileri (katalog) önbelleğe almak. Bu, her mesajda çalışan N+1 sorgu problemini de çözer.
3. Prompt'un sabit kısmını başa, değişen kısmını sona koyarak sağlayıcının prompt önbelleğinden yararlanmak.
4. Niyet çıkarma gibi basit işler için daha hafif bir model kullanmak.
5. Tool calling'e geçerek kataloğu prompt'tan tamamen çıkarmak.

### 5.4 Chatbot'ta bulunan somut hatalar
- `getMonth()` sadece Ocak–Haziran'ı tanıyordu. Temmuz–Aralık sessizce Ocak'a dönüşüyordu. Yıl da `2026` olarak sabit yazılmıştı.
- Mesajda "çocuk" geçtiğinde her bölüm eşleşiyordu ve listedeki ilk bölüm seçiliyordu.
- İstenen doktor bulunamazsa randevu sessizce bölümdeki ilk doktora veriliyordu (`orElse(doctors.get(0))`).
- "Randevunuz oluşturuldu" mesajı, randevu oluşturulmadan **önce** model tarafından üretiliyordu. Mesaj gerçek sonuçla uyuşmayabiliyordu. Onay her zaman gerçek kayıttan üretilmeli.
- Dış API çağrısında timeout yoktu.
- Bütün `catch` blokları hataları loglamadan yutuyordu. Yanlış API key, dolmuş kota ya da ağ hatası loglarda hiçbir iz bırakmıyordu.

### 5.5 Gözlemlenebilirlik ve test
- Modele ne gönderildiği `System.out` ile değil, istendiğinde açılabilen `log.debug` ile görülmeli.
- LLM'li sistemlerin asıl testi bir **eval setidir**: 20–30 örnek konuşma ve her biri için beklenen araç çağrıları. Prompt ya da model değiştiğinde bu set tekrar çalıştırılır.

---

## 6. Test ve proje altyapısı

- **Uygulama sınıfının paketi:** `@SpringBootApplication` sınıfı bir alt pakette (`starter`) duruyordu. Spring Boot Test, konfigürasyon sınıfını testin paketinden yukarı doğru arar, bu yüzden testler baştan beri hiç çalışmıyordu. Ana sınıf kök pakette olmalı. O zaman `scanBasePackages` ve `@ComponentScan` gibi elle yazılmış ayarlara da gerek kalmaz.
- **Test konfigürasyonu:** H2 URL'i hatalıydı (`jdbc:h2://mem:db`), gereken bir property tanımlı değildi ve test veritabanı production şemasını taklit etmeye çalışıyordu.
- **Boş testler:** Repository testindeki 5 test metodunun gövdesi boştu. "Geçen" ama hiçbir şeyi doğrulamayan bir test, hiç test olmamasından daha yanıltıcıdır.
- **Eksik test türü:** Endpoint'leri gerçekten çağıran birkaç `MockMvc` testi, yetkilendirme katmanındaki iki hatayı da hemen yakalardı.

---

## 7. AI ile kod yazarken dikkat edilecekler

Bu projede görülen ve AI tarafından üretilmiş koda özgü olan kalıplar:

- **Kalıbın mekanik uygulanması:** Her yere interface, her yere `@Override`, gerek olmadığı hâlde base class. Kalıp uygulanmış, arkasındaki gerekçe sorgulanmamış.
- **Yarım kalan özellikler:** Alanları ve kontrolleri olan ama hiç bağlanmamış hesap kilidi, entity'leri olan ama hiç kullanılmayan sohbet kaydı, alanı olan ama hiç kullanılmayan soft-delete.
- **Katman katman eklenen kararlar:** Önceki bir kararın (header ile auth) kalıntısı temizlenmeden yeni bir karar (cookie ile auth) eklenmiş.
- **Yanlış yere yapıştırılmış kod:** `jdk.jfr.SettingDefinition` gibi alakasız import'lar, `"çocuk"` koşulunun yanlış yere eklenmesi, ay listesinin yarıda kesilmesi.
- **Görev notu olarak kalmış yorumlar:** "KRİTİK DÜZELTME", "EKLENDİ", "yolu 'users' olarak güncelledik". Bu bilgiler commit mesajına aittir, koda değil.
- **Kendinden emin ama yanlış varsayımlar:** Rol öneki, `BeanUtils`'in alan eşleştirmesi, `@PreAuthorize`'ın kendiliğinden çalıştığı varsayımı.

AI ile çalışırken, bu gözden geçirmede de olduğu gibi:
- Her önerinin **neden** doğru olduğunu sor. Gerekçesi açıklanamayan kalıbı kabul etme.
- Değişiklikten sonra **uçtan uca doğrula**. "Derleniyor" ve "testler geçiyor", "çalışıyor" anlamına gelmez. Bu gözden geçirmede de bir güvenlik ayarı "tek satırlık düzeltme" ile açıldı ve gizli bir hatayla birleşince uygulamanın büyük kısmını kilitledi. Bu, ancak rol kontrolleri tek tek incelenince fark edildi.
- Bir özelliği değiştirdiğinde (silme stratejisi, hata yönetimi) o özelliğe **dokunan bütün yolları** kontrol et.

---

## 8. Yeni bir proje için kontrol listesi

**Güvenlik**
- [ ] Secret'lar git'e hiç girmiyor, ortam değişkenlerinden okunuyor
- [ ] Token saklama stratejisi (cookie ya da header) tek ve net
- [ ] `@EnableMethodSecurity` açık, rol önekleri doğru ve en az bir endpoint testi bunu doğruluyor
- [ ] Her endpoint'te "bu kayıt bu kullanıcıya mı ait" kontrolü var
- [ ] Güncelleme DTO'larında rol ya da durum gibi hassas alanlar yok
- [ ] Kimlik her zaman `SecurityContext`'ten alınıyor, client'tan değil

**Tasarım**
- [ ] Her bilginin ve her kuralın tek bir kaynağı var
- [ ] Interface sadece gerçek bir ihtiyaç varsa ekleniyor
- [ ] Framework sözleşmeleri domain modelinden ayrı tutuluyor
- [ ] Silme stratejisine karar verilmiş ve tutarlı uygulanıyor, cascade'ler bilinçli seçiliyor

**API**
- [ ] Başarı ve hata aynı response şeklinde dönüyor
- [ ] HTTP status kodları anlamlı
- [ ] Validasyon, iş ve güvenlik hataları ayrı ayrı ele alınıyor
- [ ] Response DTO'larındaki her alanı dolduran bir kod var

**LLM**
- [ ] Modelin sorumluluğu dar tutuluyor, kararları ve doğrulamaları kod veriyor
- [ ] Yazma işlemleri açık bir onay adımından geçiyor
- [ ] Konuşma geçmişi sunucuda tutuluyor ve sınırlandırılıyor
- [ ] Dış API çağrılarında timeout, loglama ve maliyet sınırları var
- [ ] Bir eval seti var

**Altyapı**
- [ ] Ana uygulama sınıfı kök pakette
- [ ] Testler gerçekten çalışıyor ve bir şeyleri doğruluyor
- [ ] Şema değişiklikleri bir migration aracıyla yönetiliyor
