package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.AnswerOptionDto;
import back.client_exp_backend.dto.QuestionDto;
import back.client_exp_backend.exception.ResourceNotFoundException;
import back.client_exp_backend.models.AnswerOption;
import back.client_exp_backend.models.Question;
import back.client_exp_backend.models.Survey;
import back.client_exp_backend.models.enums.QuestionType;
import back.client_exp_backend.repository.QuestionRepository;
import back.client_exp_backend.repository.SurveyRepository;
import back.client_exp_backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

  private final QuestionRepository questionRepository;
  private final SurveyRepository surveyRepository;

  @Override
  public List<QuestionDto> getAllQuestions() {
    List<Question> questions = questionRepository.findAll();
    return questions.stream()
        .map(this::mapEntityToDto)
        .collect(Collectors.toList());
  }

  @Override
  public QuestionDto getQuestionById(Long id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Вопрос с ID " + id + " не найден"));
    return mapEntityToDto(question);
  }

  @Override
  @Transactional
  public QuestionDto createQuestion(QuestionDto questionDto) {
    Question question = mapDtoToEntity(questionDto);
    Question savedQuestion = questionRepository.save(question);
    return mapEntityToDto(savedQuestion);
  }

  @Override
  @Transactional
  public QuestionDto updateQuestion(Long id, QuestionDto questionDto) {
    Question existingQuestion = questionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Вопрос с ID " + id + " не найден"));

    // Обновляем поля
    existingQuestion.setText(questionDto.getText());
    existingQuestion.setType(QuestionType.valueOf(questionDto.getType()));
    existingQuestion.setRequired(questionDto.getRequired());
    existingQuestion.setCategory(questionDto.getCategory());
    existingQuestion.setMetricType(questionDto.getMetricType());

    if (questionDto.getOrderNumber() != null) {
      existingQuestion.setOrderNumber(questionDto.getOrderNumber());
    }

    // Обновляем варианты ответов, если у вопроса есть опции
    if (questionDto.getOptions() != null && !questionDto.getOptions().isEmpty()) {
      existingQuestion.getAnswerOptions().clear();
      List<AnswerOption> options = new ArrayList<>();

      for (int i = 0; i < questionDto.getOptions().size(); i++) {
        String optionText = questionDto.getOptions().get(i);
        AnswerOption option = new AnswerOption();
        option.setText(optionText);
        option.setOrderNumber(i + 1); // Устанавливаем порядковый номер
        option.setQuestion(existingQuestion);
        options.add(option);
      }

      existingQuestion.getAnswerOptions().addAll(options);
    }

    Question updatedQuestion = questionRepository.save(existingQuestion);
    return mapEntityToDto(updatedQuestion);
  }

  @Override
  @Transactional
  public void deleteQuestion(Long id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Вопрос с ID " + id + " не найден"));
    questionRepository.delete(question);
  }

  private Question mapDtoToEntity(QuestionDto dto) {
    Question question = new Question();
    question.setText(dto.getText());
    question.setType(QuestionType.valueOf(dto.getType()));
    question.setRequired(dto.getRequired());
    question.setCategory(dto.getCategory());
    question.setMetricType(dto.getMetricType());

    if (dto.getSurveyId() != null) {
      Survey survey = surveyRepository.findById(dto.getSurveyId())
          .orElseThrow(() -> new ResourceNotFoundException("Опрос с ID " + dto.getSurveyId() + " не найден"));
      question.setSurvey(survey);
    }

    if (dto.getOrderNumber() != null) {
      question.setOrderNumber(dto.getOrderNumber());
    } else {
      question.setOrderNumber(1); // Значение по умолчанию
    }

    // Если у вопроса есть варианты ответов
    if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
      List<AnswerOption> options = new ArrayList<>();

      for (int i = 0; i < dto.getOptions().size(); i++) {
        String optionText = dto.getOptions().get(i);
        AnswerOption option = new AnswerOption();
        option.setText(optionText);
        option.setOrderNumber(i + 1); // Устанавливаем порядковый номер
        option.setQuestion(question);
        options.add(option);
      }

      question.setAnswerOptions(options);
    }

    return question;
  }

  private QuestionDto mapEntityToDto(Question entity) {
    QuestionDto dto = new QuestionDto();
    dto.setId(entity.getId());

    if (entity.getSurvey() != null) {
      dto.setSurveyId(entity.getSurvey().getId());
    }

    dto.setText(entity.getText());
    dto.setType(entity.getType().name());
    dto.setRequired(entity.isRequired());
    dto.setCategory(entity.getCategory());
    dto.setMetricType(entity.getMetricType());
    dto.setOrderNumber(entity.getOrderNumber());
    dto.setCreatedAt(entity.getCreatedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());

    if (entity.getAnswerOptions() != null && !entity.getAnswerOptions().isEmpty()) {
      List<AnswerOptionDto> optionDtos = entity.getAnswerOptions().stream()
          .map(this::mapAnswerOptionToDto)
          .collect(Collectors.toList());
      dto.setAnswerOptions(optionDtos);
    }

    return dto;
  }

  private AnswerOptionDto mapAnswerOptionToDto(AnswerOption option) {
    return AnswerOptionDto.builder()
        .id(option.getId())
        .text(option.getText())
        .orderNumber(option.getOrderNumber())
        .build();
  }
}