package com.cvanalyzer.repos;

import com.cvanalyzer.entities.CvUpload;
import com.cvanalyzer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CvUploadRepository extends JpaRepository<CvUpload, Long> {
    List<CvUpload> findByUser(User user);

    long countByUser(User user);

    boolean existsByUserAndFileName(User user, String Filename);
}
