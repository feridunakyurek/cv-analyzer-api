package com.cvanalyzer.controllers;

import com.cvanalyzer.entities.Evaluation;
import com.cvanalyzer.exceptions.UserNotFoundException;
import com.cvanalyzer.services.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluations")
public class EvaluationController {

    private final EvaluationService service;

    public EvaluationController(EvaluationService service) {
        this.service = service;
    }

    @PostMapping("/analyze/{cvId}")
    public ResponseEntity<Evaluation> analyzeCv(@PathVariable Long cvId, Authentication authentication) throws UserNotFoundException, IOException {
        String userEmail =  authentication.getName();

        Evaluation evaluation = service.analyzeCvAndVerifyUser(cvId, userEmail);
        return ResponseEntity.ok(evaluation);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Evaluation>> getUserEvaluations(Authentication authentication) throws UserNotFoundException {
        String  userEmail =  authentication.getName();

        List<Evaluation> evaluations = service.getByUserEmail(userEmail);

        return ResponseEntity.ok(evaluations);
    }

    @GetMapping("/analyze/{cvId}")
    public ResponseEntity<?> getEvaluationByCv(@PathVariable Long cvId) {
        Evaluation evaluation = service.getEvaluationByCvId(cvId);
        if (evaluation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(evaluation);
    }

    @DeleteMapping("/delete/{cvId}")
    public ResponseEntity<Void> deleteCv(@PathVariable Long cvId) {

        service.deleteEvaluation(cvId);

        return ResponseEntity.noContent().build();
    }
}
