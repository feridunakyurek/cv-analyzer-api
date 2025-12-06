# CV Analysis API

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.1-green) ![Spring AI](https://img.shields.io/badge/Spring_AI-1.0.0_M3-magenta) ![Apache Tika](https://img.shields.io/badge/Parser-Apache_Tika-yellow) ![Security](https://img.shields.io/badge/Security-JWT-red) ![Database](https://img.shields.io/badge/Database-MySQL-blue)

### 🛠️ Bu proje aşağıdaki teknolojiler üzerine inşa edilmiştir (Bkz: `pom.xml`):

* **Core:** Java 17, Spring Boot 3.3.1
* **AI & LLM:** Spring AI (Gemini 2.0 Flash Lite) (OpenAI Interface üzerinden Gemini bağlantısı)
* **Dosya Okuma:** Apache Tika 2.9.1
* **Güvenlik:** Spring Security, JJWT (0.11.5)
* **Veritabanı:** MySQL 8, Spring Data JPA
* **Araçlar:** Lombok, Maven, DevTools
---

## ⚙️ Kurulum ve Ayarlar

Projeyi çalıştırmadan önce aşağıdaki ayarları yapmanız gerekmektedir.

### 1. Veritabanı Oluşturma
MySQL sunucunuzda aşağıdaki komutla boş bir veritabanı açın:

```sql
CREATE DATABASE `cv-evaluation`;
```
*Tablolar uygulama ilk açıldığında Hibernate tarafından otomatik oluşturulacaktır*

## 2.Ortam Değişkenleri (Environment Variables) ⚠️
Güvenlik nedeniyle API anahtarları kod içerisine gömülmemiştir. Projeyi çalıştırmadan önce GEMINI_API_KEY ve JwtToken tanımlanmalıdır.

**IntelliJ IDEA Kullanıyorsanız:**
* Run/Debug Configurations menüsünü açın.
* Environment variables alanına şunu ekleyin: GEMINI_API_KEY=*API_KEY*
 
**Terminalde Çalıştırıyorsanız:**
* Mac/Linux
```bash
export GEMINI_API_KEY=API_KEY
```
* Windows (CMD)
```bash
set GEMINI_API_KEY=API_KEY
```

**IntelliJ IDEA Kullanıyorsanız:**
* Run/Debug Configurations menüsünü açın.
* Environment variables alanına şunu ekleyin: JwtToken=*API_KEY*
 
**Terminalde Çalıştırıyorsanız:**
* Mac/Linux
```bash
export JwtToken=API_KEY
```
* Windows (CMD)
```bash
set JwtToken=API_KEY
```

## 3.application.properties Kontrolü
Varsayılan ayarlar aşağıdadır. Kendi veritabanı şifrenize göre güncelleyebilirsiniz:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cv-evaluation
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.servlet.multipart.max-file-size=5MB
```

##  4.Çalıştırma 
```bash
mvn clean install
mvn spring-boot:run
```
Başarılı olduğunda konsolda Tomcat started on port 8080 yazısını göreceksiniz.



## 3.API Kullanımı ve Test Senaryosu
Uygulama çalıştığında Swagger arayüzüne gidin: 👉 http://localhost:8080/swagger-ui/index.html

### Adım adım Kullanım:

#### 1.Kayıt Ol (Register):
* AuthController > `/api/v1/auth/register` endpoint'ine gidin.
* JSON body ile (email, password, name) bilgilerinizi girip çalıştırın.

### 2. Giriş Yap (Login):
* AuthController > `/api/v1/auth/login` endpoint'ine gidin.
* Kayıt olduğunuz bilgilerle giriş yapın.
* Dönen yanıttaki accessToken değerini kopyalayın.

### 3.Yetkilendirme (Authorize):
* Swagger sayfasının sağ üstündeki Authorize butonuna tıklayın.
* Kopyaladığınız token'ı şu formatta yapıştırın: Bearer <token> (Dikkat: "Bearer" kelimesi ile token arasında bir boşluk olmalıdır).
* Authorize ve sonra Close deyin.

### 4.CV Yükle:
* cv-upload-controller >`/api/cv/upload` endpoint'ini açın.
* Dosya seçme kısmından bir PDF veya Word dosyası yükleyin.
* Execute butonuna basın.

### 5.Analiz Et:
* evaluation-controller > `[POST]/api/v1/evaluations/analyze/{cvId}` endpoint'ini açın.
* cvId kısmına `/api/cv/upload` endpoint'inden dönen cvId'sini girin.
* Execute butonuna basın.


