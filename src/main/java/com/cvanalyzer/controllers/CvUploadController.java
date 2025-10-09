package com.cvanalyzer.controllers;

import com.cvanalyzer.entities.CvUpload;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.exceptions.FileStorageException;
import com.cvanalyzer.exceptions.FileValidationException;
import com.cvanalyzer.exceptions.UserNotFoundException;
import com.cvanalyzer.repos.CvUploadRepository;
import com.cvanalyzer.repos.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv")
public class CvUploadController {

    private final Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();

    private final CvUploadRepository cvUploadRepository;
    private final UserRepository userRepository;

    public CvUploadController(CvUploadRepository cvUploadRepository, UserRepository userRepository) {
        this.cvUploadRepository = cvUploadRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file, Authentication authentication) {

        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new FileStorageException("Kullanıcı bulunamadı."));
            // 10 MB sınırı
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("Dosya boyutu 10 MB'tan büyük olamaz.");
            }
            // İzin verilen dosya tipleri
            List<String> allowedTypes = List.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            if (!allowedTypes.contains(file.getContentType())) {
                throw new RuntimeException("Sadece PDF veya Word dosyaları yüklenebilir.");
            }

            // Kullanıcının mevcut CV sayısını kontrol et
            long cvCount = cvUploadRepository.countByUser(user);
            if (cvCount >= 3) {
                throw new RuntimeException("Bir kullanıcı en fazla 3 CV yükleyebilir.");
            }

            // Benzersiz dosya adı
            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            Path filePath = this.uploadPath.resolve(fileName);
            try {
                Files.copy(file.getInputStream(), filePath);
            } catch (IOException e) {
                throw new FileStorageException("Dosya yüklenirken hata oluştu: ", e);
            }

            // 3. Dosyanın daha önce yüklenip yüklenmediğini kontrol et
            boolean alreadyExists = cvUploadRepository.existsByUserAndOriginalFileName(user, file.getOriginalFilename());
            if (alreadyExists) {
            // Eğer dosya zaten varsa hata fırlat
            throw new FileValidationException("Aynı isimde bir dosya zaten yüklendi. Lütfen dosya adını değiştirin.");
            }

            // CvUpload nesnesi oluştur
            CvUpload cvUpload = new CvUpload();
            cvUpload.setFileName(fileName);
            cvUpload.setOriginalFileName(file.getOriginalFilename());
            cvUpload.setFileType(file.getContentType());
            cvUpload.setFilePath(filePath.toString());
            cvUpload.setFileSize(file.getSize());
            cvUpload.setUploadDate(LocalDateTime.now());
            cvUpload.setUser(user);

            cvUploadRepository.save(cvUpload);

            return ResponseEntity.ok("CV başarıyla yüklendi: " + fileName);

    }

    @GetMapping("/user/my-cvs")
    public ResponseEntity<List<CvUpload>> getUserCvs(Authentication authentication) throws UserNotFoundException {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı."));

        List<CvUpload> cvs = cvUploadRepository.findByUser(user);
        return ResponseEntity.ok(cvs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id, Authentication authentication) throws UserNotFoundException {
        String userEmail = authentication.getName();

        CvUpload cvUpload = cvUploadRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("CV bulunamadı."));

        if (!cvUpload.getUser().getEmail().equals(userEmail)) {
            return ResponseEntity.status(403).build();
        }

        try {
            Path filePath = Paths.get(cvUpload.getFilePath());

            if (Files.notExists(filePath) || !Files.isReadable(filePath)) {
                throw new FileStorageException("Dosya bulunamadı veya okunabilir değil.");
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cvUpload.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new FileStorageException("Dosya yolu geçersiz.", e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCv(@PathVariable Long id) {
        CvUpload cvUpload = cvUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV bulunamadı"));

        try {
            // Dosyayı sil
            Path filePath = Paths.get(cvUpload.getFilePath());
            Files.deleteIfExists(filePath);

            // Veritabanından sil
            cvUploadRepository.delete(cvUpload);

            return ResponseEntity.ok("CV başarıyla silindi.");

        } catch (IOException e) {
            throw new FileStorageException("CV silme sırasında hata oluştu.");
        }

    }
}
