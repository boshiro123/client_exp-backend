package back.client_exp_backend.service;

import back.client_exp_backend.dto.QuestionDto;
import back.client_exp_backend.models.Question;

import java.util.List;

public interface QuestionService {
  List<QuestionDto> getAllQuestions();

  QuestionDto getQuestionById(Long id);

  QuestionDto createQuestion(QuestionDto questionDto);

  QuestionDto updateQuestion(Long id, QuestionDto questionDto);

  void deleteQuestion(Long id);
}