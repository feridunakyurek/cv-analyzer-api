package com.cvanalyzer.services;

import com.cvanalyzer.entities.CvUpload;
import com.cvanalyzer.entities.Evaluation;
import com.cvanalyzer.repos.CvUploadRepository;
import com.cvanalyzer.repos.EvaluationRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class EvaluationService {

    private final EvaluationRepository repo;

    private final CvUploadRepository cvUploadRepository;

    public EvaluationService(EvaluationRepository repo,  CvUploadRepository cvUploadRepository) {
        this.repo = repo;
        this.cvUploadRepository = cvUploadRepository;
    }

    public List<Evaluation> getByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public Evaluation analyzeCv(Long cvId) throws IOException {
        // CV'yi getir
        CvUpload cv = repo.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV bulunamadı: " + cvId)).getCvUpload();

        // CV metnini al (şimdilik resumeText olarak varsayalım)
        String resumeText = cv.getResumeText();
        if (resumeText == null || resumeText.isEmpty()) {
            throw new RuntimeException("CV içeriği boş veya okunamadı.");
        }

        // Basit AI analizi (mock)
        int wordCount = resumeText.split("\\s+").length;
        double score = Math.min(100, wordCount / 10.0 + 50);
        String comment = score > 75
                ? "Harika bir CV! Deneyimlerin ve becerilerin güçlü görünüyor."
                : "CV geliştirilebilir. Daha fazla proje ve detay eklemeyi düşün.";

        // Evaluation nesnesi oluştur
        Evaluation eval = new Evaluation();
        eval.setFileName(cv.getFileName());
        eval.setResumeText(resumeText);
        eval.setScore(score);
        eval.setAiComment(comment);
        eval.setUser(cv.getUser());

        // Kaydet
        return repo.save(eval);
    }

}
