package com.cvanalyzer.controllers;

import com.cvanalyzer.entities.Evaluation;
import com.cvanalyzer.services.EvaluationService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin(origins = "http://localhost:3000")
public class EvaluationController {

    private final EvaluationService service;

    public EvaluationController(EvaluationService service) {
        this.service = service;
    }

    @PostMapping("/analyze/{cvId}")
    public Evaluation analyzeCv(@PathVariable Long cvId) throws IOException {
        return service.analyzeCv(cvId);
    }

    @GetMapping("/{userId}")
    public List<Evaluation> getUserEvaluations(@PathVariable Long userId) {
        return service.getByUser(userId);
    }
}
