package back.client_exp_backend.repository;

import back.client_exp_backend.models.Question;
import back.client_exp_backend.models.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
  List<Question> findBySurveyOrderByOrderNumberAsc(Survey survey);

  void deleteBySurvey(Survey survey);
}