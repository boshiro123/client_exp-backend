package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.AnswerOptionDto;
import back.client_exp_backend.exception.ResourceNotFoundException;
import back.client_exp_backend.models.AnswerOption;
import back.client_exp_backend.models.Question;
import back.client_exp_backend.repository.AnswerOptionRepository;
import back.client_exp_backend.repository.QuestionRepository;
import back.client_exp_backend.service.AnswerOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerOptionServiceImpl implements AnswerOptionService {

  private final AnswerOptionRepository answerOptionRepository;
  private final QuestionRepository questionRepository;

  @Override
  public List<AnswerOptionDto> getAnswerOptionsByQuestionId(Long questionId) {
    Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new ResourceNotFoundException("Вопрос с ID " + questionId + " не найден"));

    return question.getAnswerOptions().stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  @Override
  public AnswerOptionDto getAnswerOptionById(Long id) {
    AnswerOption answerOption = answerOptionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Вариант ответа с ID " + id + " не найден"));
    return mapToDto(answerOption);
  }

  @Override
  @Transactional
  public AnswerOptionDto createAnswerOption(Long questionId, AnswerOptionDto answerOptionDto) {
    Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new ResourceNotFoundException("Вопрос с ID " + questionId + " не найден"));

    AnswerOption answerOption = new AnswerOption();
    answerOption.setText(answerOptionDto.getText());

    // Определяем порядковый номер нового варианта ответа
    Integer maxOrderNumber = question.getAnswerOptions().stream()
        .map(AnswerOption::getOrderNumber)
        .max(Integer::compare)
        .orElse(0);

    answerOption.setOrderNumber(
        answerOptionDto.getOrderNumber() != null ? answerOptionDto.getOrderNumber() : maxOrderNumber + 1);

    answerOption.setQuestion(question);

    AnswerOption savedOption = answerOptionRepository.save(answerOption);
    return mapToDto(savedOption);
  }

  @Override
  @Transactional
  public AnswerOptionDto updateAnswerOption(Long id, AnswerOptionDto answerOptionDto) {
    AnswerOption answerOption = answerOptionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Вариант ответа с ID " + id + " не найден"));

    answerOption.setText(answerOptionDto.getText());

    if (answerOptionDto.getOrderNumber() != null) {
      answerOption.setOrderNumber(answerOptionDto.getOrderNumber());
    }

    AnswerOption updatedOption = answerOptionRepository.save(answerOption);
    return mapToDto(updatedOption);
  }

  @Override
  @Transactional
  public void deleteAnswerOption(Long id) {
    AnswerOption answerOption = answerOptionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Вариант ответа с ID " + id + " не найден"));

    answerOptionRepository.delete(answerOption);
  }

  private AnswerOptionDto mapToDto(AnswerOption answerOption) {
    return AnswerOptionDto.builder()
        .id(answerOption.getId())
        .text(answerOption.getText())
        .orderNumber(answerOption.getOrderNumber())
        .build();
  }
}