package com.cvanalyzer.repos;

import com.cvanalyzer.entities.Evaluation;
import com.cvanalyzer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByUser(User user);

    List<Evaluation> findByUserId(Long userId);
}
