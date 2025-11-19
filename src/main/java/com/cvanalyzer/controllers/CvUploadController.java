package com.cvanalyzer.controllers;

import com.cvanalyzer.entities.CvUpload;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.exceptions.*;
import com.cvanalyzer.repos.CvUploadRepository;
import com.cvanalyzer.repos.EvaluationRepository;
import com.cvanalyzer.repos.UserRepository;
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

@RestController
@RequestMapping("/api/cv")
public class CvUploadController {

    private final Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();

    private final CvUploadRepository cvUploadRepository;
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;

    public CvUploadController(CvUploadRepository cvUploadRepository, UserRepository userRepository, EvaluationRepository evaluationRepository) {
        this.cvUploadRepository = cvUploadRepository;
        this.userRepository = userRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file, Authentication authentication) throws UserNotFoundException {

        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı."));

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileValidationException("Dosya boyutu 5 MB'tan büyük olamaz.");
        }

        List<String> allowedTypes = List.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        if (!allowedTypes.contains(file.getContentType())) {
            throw new FileValidationException("Sadece PDF veya Word dosyaları yüklenebilir.");
        }

        long cvCount = cvUploadRepository.countByUser(user);
        if (cvCount >= 3) {
            throw new FileValidationException("Bir kullanıcı en fazla 3 CV yükleyebilir.");
        }

        boolean alreadyExists = cvUploadRepository.existsByUserAndFileName(user, file.getOriginalFilename());
        if (alreadyExists) {
            throw new FileValidationException("Aynı isimde bir dosya zaten yüklendi. Lütfen dosya adını değiştirin.");
        }

        String fileName = file.getOriginalFilename();
        assert fileName != null;
        Path filePath = this.uploadPath.resolve(fileName);

        try {
            if (!Files.exists(this.uploadPath)) {
                Files.createDirectories(this.uploadPath);
            }
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new FileStorageException("Dosya yüklenirken hata oluştu.", e);
        }

        CvUpload cvUpload = new CvUpload();
        cvUpload.setFileName(fileName);
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
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();

        CvUpload cvUpload = cvUploadRepository.findById(id)
                .orElseThrow(() -> new CvNotFoundException("CV bulunamadı."));

        if (!cvUpload.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedAccessException("Bu CV'ye erişim yetkiniz yok.");
        }

        try {
            Path filePath = Paths.get(cvUpload.getFilePath());

            if (Files.notExists(filePath) || !Files.isReadable(filePath)) {
                throw new FileStorageException("Dosya bulunamadı veya okunabilir değil.");
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new FileStorageException("Dosya yolu geçersiz.", e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCv(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();

        CvUpload cvUpload = cvUploadRepository.findById(id)
                .orElseThrow(() -> new CvNotFoundException("CV bulunamadı."));

        if (!cvUpload.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedAccessException("Bu CV'yi silme yetkiniz yok.");
        }

        try {
            evaluationRepository.deleteByCvUpload(cvUpload);
            Path filePath = Paths.get(cvUpload.getFilePath());
            Files.deleteIfExists(filePath);
            cvUploadRepository.delete(cvUpload);
            return ResponseEntity.ok("CV başarıyla silindi.");
        } catch (IOException e) {
            throw new FileStorageException("CV silme sırasında hata oluştu.", e);
        }
    }
}