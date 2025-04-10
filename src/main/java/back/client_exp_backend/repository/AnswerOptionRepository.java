package back.client_exp_backend.repository;

import back.client_exp_backend.models.AnswerOption;
import back.client_exp_backend.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
  List<AnswerOption> findByQuestionOrderByOrderNumberAsc(Question question);

  void deleteByQuestion(Question question);
}