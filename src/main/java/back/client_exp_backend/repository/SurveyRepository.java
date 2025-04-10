package back.client_exp_backend.repository;

import back.client_exp_backend.models.Survey;
import back.client_exp_backend.models.User;
import back.client_exp_backend.models.enums.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
  Page<Survey> findByStatus(SurveyStatus status, Pageable pageable);

  Page<Survey> findByCreatedBy(User user, Pageable pageable);

  Page<Survey> findByStatusAndCreatedBy(SurveyStatus status, User user, Pageable pageable);
}