package com.cvanalyzer.services;

import com.cvanalyzer.entities.CvUpload;
import com.cvanalyzer.entities.Evaluation;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.exceptions.CvNotFoundException;
import com.cvanalyzer.exceptions.UnauthorizedAccessException;
import com.cvanalyzer.exceptions.UserNotFoundException;
import com.cvanalyzer.repos.CvUploadRepository;
import com.cvanalyzer.repos.EvaluationRepository;
import com.cvanalyzer.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Optional;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final CvUploadRepository cvUploadRepository;
    private final UserRepository userRepository;

    public EvaluationService(EvaluationRepository evaluationRepository, CvUploadRepository cvUploadRepository, UserRepository userRepository) {
        this.evaluationRepository = evaluationRepository;
        this.cvUploadRepository = cvUploadRepository;
        this.userRepository = userRepository;
    }

    public Evaluation analyzeCvAndVerifyUser(Long cvId, String userEmail) throws UserNotFoundException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı."));

        CvUpload cvUpload = cvUploadRepository.findById(cvId)
                .orElseThrow(() -> new CvNotFoundException("CV bulunamadı."));

        if (!cvUpload.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedAccessException("Bu CV'yi analiz etme yetkiniz yok.");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setScore(new Random().nextInt(101)); // Örnek puan (0-100 arası)
        evaluation.setAnalysisSummary("Bu bir örnek değerlendirme özetidir.");
        evaluation.setCreatedAt(LocalDateTime.now());
        evaluation.setEvaluationType("AI_ANALYSIS");
        evaluation.setCvUpload(cvUpload);
        evaluation.setUser(user);

        return evaluationRepository.save(evaluation);
    }

    public List<Evaluation> getByUserEmail(String userEmail) throws UserNotFoundException {
        // Kullanıcıyı bul
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı."));

        // Kullanıcının tüm değerlendirmelerini getir
        return evaluationRepository.findByUser(user);
    }
}