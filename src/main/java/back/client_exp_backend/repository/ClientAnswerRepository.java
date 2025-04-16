package back.client_exp_backend.repository;

import back.client_exp_backend.models.Client;
import back.client_exp_backend.models.ClientAnswer;
import back.client_exp_backend.models.Question;
import back.client_exp_backend.models.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientAnswerRepository extends JpaRepository<ClientAnswer, Long> {
  List<ClientAnswer> findByClient(Client client);

  List<ClientAnswer> findBySurvey(Survey survey);

  List<ClientAnswer> findByClientAndSurvey(Client client, Survey survey);

  List<ClientAnswer> findByQuestion(Question question);

  @Query("SELECT COUNT(ca) FROM ClientAnswer ca WHERE ca.survey.id = :surveyId AND ca.client.id = :clientId")
  int countBySurveyIdAndClientId(Long surveyId, Long clientId);

  @Query("SELECT DISTINCT ca.client.id FROM ClientAnswer ca WHERE ca.survey.id = :surveyId")
  List<Long> findDistinctClientIdsBySurveyId(@Param("surveyId") Long surveyId);

  @Query("SELECT COUNT(DISTINCT ca.client.id) FROM ClientAnswer ca WHERE ca.survey.id = :surveyId")
  Long countDistinctClientsBySurveyId(@Param("surveyId") Long surveyId);
}