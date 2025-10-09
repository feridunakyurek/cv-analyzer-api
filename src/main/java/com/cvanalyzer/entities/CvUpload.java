package com.cvanalyzer.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String originalFileName;

    private String fileType;

    private String filePath;

    private Long fileSize;

    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public String resumeText;
}
