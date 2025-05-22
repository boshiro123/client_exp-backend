package back.client_exp_backend.service;

import back.client_exp_backend.dto.AnswerOptionDto;
import java.util.List;

public interface AnswerOptionService {
  List<AnswerOptionDto> getAnswerOptionsByQuestionId(Long questionId);

  AnswerOptionDto getAnswerOptionById(Long id);

  AnswerOptionDto createAnswerOption(Long questionId, AnswerOptionDto answerOptionDto);

  AnswerOptionDto updateAnswerOption(Long id, AnswerOptionDto answerOptionDto);

  void deleteAnswerOption(Long id);
}