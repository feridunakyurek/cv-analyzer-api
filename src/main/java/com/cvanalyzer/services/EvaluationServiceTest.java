package com.cvanalyzer.services;

import com.cvanalyzer.entities.CvUpload;
import com.cvanalyzer.entities.Evaluation;
import com.cvanalyzer.entities.User;
import com.cvanalyzer.exceptions.CvNotFoundException;
import com.cvanalyzer.exceptions.EvaluationAlreadyExistsException;
import com.cvanalyzer.exceptions.UnauthorizedAccessException;
import com.cvanalyzer.exceptions.UserNotFoundException;
import com.cvanalyzer.repos.CvUploadRepository;
import com.cvanalyzer.repos.EvaluationRepository;
import com.cvanalyzer.repos.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private CvUploadRepository cvUploadRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        evaluationService = new EvaluationService(
                evaluationRepository,
                cvUploadRepository,
                userRepository,
                chatClientBuilder,
                new ObjectMapper()
        );
    }

    // ❌ Kullanıcı bulunamadı
    @Test
    void analyzeCv_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("yok@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> evaluationService.analyzeCvAndVerifyUser(1L, "yok@test.com"));

        verify(cvUploadRepository, never()).findById(any());
    }

    // ❌ CV bulunamadı
    @Test
    void analyzeCv_WhenCvNotFound_ShouldThrowException() {
        User user = new User();
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cvUploadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CvNotFoundException.class,
                () -> evaluationService.analyzeCvAndVerifyUser(99L, "test@test.com"));
    }

    // ❌ CV başka kullanıcıya ait
    @Test
    void analyzeCv_WhenCvBelongsToAnotherUser_ShouldThrowException() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        User requester = new User();
        requester.setEmail("requester@test.com");

        CvUpload cvUpload = new CvUpload();
        cvUpload.setUser(owner);

        when(userRepository.findByEmail("requester@test.com")).thenReturn(Optional.of(requester));
        when(cvUploadRepository.findById(1L)).thenReturn(Optional.of(cvUpload));

        assertThrows(UnauthorizedAccessException.class,
                () -> evaluationService.analyzeCvAndVerifyUser(1L, "requester@test.com"));
    }

    // ❌ CV zaten değerlendirilmiş
    @Test
    void analyzeCv_WhenEvaluationAlreadyExists_ShouldThrowException() {
        User user = new User();
        user.setEmail("test@test.com");

        CvUpload cvUpload = new CvUpload();
        cvUpload.setUser(user);

        Evaluation existing = new Evaluation();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(cvUploadRepository.findById(1L)).thenReturn(Optional.of(cvUpload));
        when(evaluationRepository.findByCvUpload(cvUpload)).thenReturn(Optional.of(existing));

        assertThrows(EvaluationAlreadyExistsException.class,
                () -> evaluationService.analyzeCvAndVerifyUser(1L, "test@test.com"));
    }

    // ✅ Kullanıcıya ait değerlendirmeler listelendi
    @Test
    void getByUserEmail_WhenUserExists_ShouldReturnEvaluations() throws UserNotFoundException {
        User user = new User();
        user.setEmail("test@test.com");

        Evaluation e1 = new Evaluation();
        Evaluation e2 = new Evaluation();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(evaluationRepository.findByUser(user)).thenReturn(List.of(e1, e2));

        List<Evaluation> result = evaluationService.getByUserEmail("test@test.com");

        assertEquals(2, result.size());
        verify(evaluationRepository, times(1)).findByUser(user);
    }

    // ❌ Kullanıcı bulunamadı — getByUserEmail
    @Test
    void getByUserEmail_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("yok@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> evaluationService.getByUserEmail("yok@test.com"));
    }

    // ✅ CV'ye ait değerlendirme bulundu
    @Test
    void getEvaluationByCvId_WhenExists_ShouldReturnEvaluation() {
        Evaluation evaluation = new Evaluation();
        evaluation.setScore(85.0);

        when(evaluationRepository.findByCvUpload_Id(1L)).thenReturn(Optional.of(evaluation));

        Evaluation result = evaluationService.getEvaluationByCvId(1L);

        assertNotNull(result);
        assertEquals(85.0, result.getScore());
    }

    // ❌ CV'ye ait değerlendirme bulunamadı
    @Test
    void getEvaluationByCvId_WhenNotExists_ShouldThrowException() {
        when(evaluationRepository.findByCvUpload_Id(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationService.getEvaluationByCvId(99L));
    }

    // ✅ Değerlendirme silindi
    @Test
    void deleteEvaluation_WhenCvExists_ShouldDelete() {
        CvUpload cvUpload = new CvUpload();

        when(cvUploadRepository.findById(1L)).thenReturn(Optional.of(cvUpload));

        evaluationService.deleteEvaluation(1L);

        verify(evaluationRepository, times(1)).deleteByCvUpload(cvUpload);
    }

    // ❌ Silinecek CV bulunamadı
    @Test
    void deleteEvaluation_WhenCvNotFound_ShouldThrowException() {
        when(cvUploadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CvNotFoundException.class,
                () -> evaluationService.deleteEvaluation(99L));

        verify(evaluationRepository, never()).deleteByCvUpload(any());
    }
}