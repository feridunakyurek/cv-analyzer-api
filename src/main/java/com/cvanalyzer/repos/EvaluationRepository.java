package com.cvanalyzer.repos;

import com.cvanalyzer.entities.CvUpload;
import com.cvanalyzer.entities.Evaluation;
import com.cvanalyzer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByUser(User user);

    List<Evaluation> findByUserId(Long userId);

    Optional<Evaluation> findByCvUpload_Id(Long cvUpload);
    void deleteByCvUpload(CvUpload cvUpload);

    Optional<Evaluation> findByCvUpload(CvUpload cvUpload);
}
