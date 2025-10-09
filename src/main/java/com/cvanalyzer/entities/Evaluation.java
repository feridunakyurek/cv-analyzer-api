package com.cvanalyzer.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double score;

    @Column(length = 2000)
    private String analysisSummary;

    private LocalDateTime createdAt;

    private String evaluationType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "cv_id", nullable = false)
    private CvUpload cvUpload;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}