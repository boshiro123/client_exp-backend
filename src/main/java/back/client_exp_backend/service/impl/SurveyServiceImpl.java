package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.PagedResponseDto;
import back.client_exp_backend.dto.QuestionDto;
import back.client_exp_backend.dto.SurveyDto;
import back.client_exp_backend.dto.SurveyStatusUpdateRequest;
import back.client_exp_backend.dto.UserInfoDto;
import back.client_exp_backend.exception.ResourceNotFoundException;
import back.client_exp_backend.models.*;
import back.client_exp_backend.models.enums.QuestionType;
import back.client_exp_backend.models.enums.SurveyStatus;
import back.client_exp_backend.repository.AnswerOptionRepository;
import back.client_exp_backend.repository.QuestionRepository;
import back.client_exp_backend.repository.SurveyRepository;
import back.client_exp_backend.service.SurveyService;
import back.client_exp_backend.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyServiceImpl implements SurveyService {

  private final SurveyRepository surveyRepository;
  private final QuestionRepository questionRepository;
  private final AnswerOptionRepository answerOptionRepository;
  private final UserService userService;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @Transactional
  public SurveyDto createSurvey(SurveyDto surveyDto, String userEmail) {
    log.info("Создание нового опросника от пользователя {}", userEmail);
    validateSurvey(surveyDto);

    // Находим пользователя по email
    User user = userService.findByEmail(userEmail);
    if (user == null) {
      throw new ResourceNotFoundException("Пользователь с email " + userEmail + " не найден");
    }
    log.debug("Найден пользователь: {}", user.getUsername());

    // Преобразуем статус из строки в enum, если нужно
    SurveyStatus status = getSurveyStatus(surveyDto);
    log.debug("Определен статус опросника: {}", status);

    // Создаем объект опросника из DTO
    Survey survey = Survey.builder()
        .title(surveyDto.getTitle())
        .description(surveyDto.getDescription())
        .status(status)
        .startDate(surveyDto.getStartDate())
        .endDate(surveyDto.getEndDate())
        .createdBy(user)
        .questions(new ArrayList<>())
        .build();

    // Сохраняем опросник
    Survey savedSurvey = surveyRepository.save(survey);
    log.debug("Сохранен опросник с ID: {}", savedSurvey.getId());

    // Создаем вопросы для опросника
    List<Question> questions = createQuestionsForSurvey(surveyDto.getQuestions(), savedSurvey);
    savedSurvey.setQuestions(questions);
    log.debug("Добавлено вопросов: {}", questions.size());

    // Преобразуем сохраненный опросник в DTO и возвращаем
    SurveyDto result = mapToDto(savedSurvey);
    log.debug("Опросник успешно создан и преобразован в DTO");
    return result;
  }

  private SurveyStatus getSurveyStatus(SurveyDto surveyDto) {
    if (surveyDto.getStatus() != null) {
      try {
        return SurveyStatus.valueOf(surveyDto.getStatus().toUpperCase());
      } catch (IllegalArgumentException e) {
        log.warn("Неверный статус опросника: {}", surveyDto.getStatus());
        throw new IllegalArgumentException("Неверный статус опросника: " + surveyDto.getStatus());
      }
    }
    return SurveyStatus.DRAFT; // По умолчанию - черновик
  }

  private void validateSurvey(SurveyDto surveyDto) {
    // Проверка названия
    if (surveyDto.getTitle() == null || surveyDto.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Название опросника не может быть пустым");
    }

    // Проверка дат
    if (surveyDto.getStartDate() != null && surveyDto.getEndDate() != null &&
        surveyDto.getEndDate().isBefore(surveyDto.getStartDate())) {
      throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
    }

    // Проверка вопросов
    if (surveyDto.getQuestions() == null || surveyDto.getQuestions().isEmpty()) {
      throw new IllegalArgumentException("Опросник должен содержать хотя бы один вопрос");
    }

    // Проверка типов вопросов
    for (QuestionDto questionDto : surveyDto.getQuestions()) {
      if (questionDto.getType() == null || questionDto.getType().trim().isEmpty()) {
        throw new IllegalArgumentException("Тип вопроса не может быть пустым");
      }
      if (questionDto.getText() == null || questionDto.getText().trim().isEmpty()) {
        throw new IllegalArgumentException("Текст вопроса не может быть пустым");
      }
    }
  }

  private List<Question> createQuestionsForSurvey(List<QuestionDto> questionDtos, Survey survey) {
    List<Question> questions = new ArrayList<>();
    int orderNumber = 1;

    for (QuestionDto questionDto : questionDtos) {
      try {
        log.debug("Обработка вопроса: {}", questionDto.getText());
        QuestionType questionType = convertStringToQuestionType(questionDto.getType());

        Question question = Question.builder()
            .survey(survey)
            .text(questionDto.getText())
            .type(questionType)
            .required(questionDto.getRequired())
            .orderNumber(orderNumber++)
            .answerOptions(new ArrayList<>())
            .build();

        Question savedQuestion = questionRepository.save(question);
        log.debug("Сохранён вопрос с ID: {}", savedQuestion.getId());

        // Обрабатываем варианты ответов, если они есть
        if (questionDto.getOptions() != null && !questionDto.getOptions().isEmpty()) {
          log.debug("Добавление вариантов ответов для вопроса ID: {}, количество: {}",
              savedQuestion.getId(), questionDto.getOptions().size());
          createAnswerOptionsForQuestion(questionDto.getOptions(), savedQuestion);

          // Проверяем, что варианты ответов действительно добавлены
          log.debug("После добавления вариантов ответов: {}",
              savedQuestion.getAnswerOptions() != null ? savedQuestion.getAnswerOptions().size() : "null");
        }

        questions.add(savedQuestion);
      } catch (Exception e) {
        log.error("Ошибка при создании вопроса: {}", e.getMessage(), e);
        throw e;
      }
    }

    return questions;
  }

  private QuestionType convertStringToQuestionType(String typeString) {
    if (typeString == null) {
      return null;
    }

    String type = typeString.toUpperCase().replace("_", "").trim();

    switch (type) {
      case "SINGLECHOICE":
      case "SINGLE":
        return QuestionType.SINGLE_CHOICE;
      case "MULTIPLECHOICE":
      case "MULTIPLE":
        return QuestionType.MULTIPLE_CHOICE;
      case "TEXT":
      case "ТЕКСТ":
        return QuestionType.TEXT;
      case "RATING":
      case "РЕЙТИНГ":
        return QuestionType.RATING;
      default:
        try {
          return QuestionType.valueOf(type);
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Неизвестный тип вопроса: " + typeString);
        }
    }
  }

  private void createAnswerOptionsForQuestion(List<String> optionTexts, Question question) {
    int orderNumber = 1;
    List<AnswerOption> options = new ArrayList<>();

    for (String optionText : optionTexts) {
      AnswerOption answerOption = AnswerOption.builder()
          .question(question)
          .text(optionText)
          .orderNumber(orderNumber++)
          .build();

      AnswerOption savedOption = answerOptionRepository.save(answerOption);
      options.add(savedOption);
    }

    // Убеждаемся, что коллекция инициализирована
    if (question.getAnswerOptions() == null) {
      question.setAnswerOptions(new ArrayList<>());
    }

    // Добавляем сохраненные варианты ответов в коллекцию вопроса
    question.getAnswerOptions().addAll(options);
  }

  private SurveyDto mapToDto(Survey survey) {
    List<QuestionDto> questionDtos = new ArrayList<>();

    try {
      if (survey.getQuestions() != null) {
        questionDtos = survey.getQuestions().stream()
            .sorted((q1, q2) -> q1.getOrderNumber().compareTo(q2.getOrderNumber()))
            .map(this::mapQuestionToDto)
            .collect(Collectors.toList());
      } else {
        log.warn("Отсутствуют вопросы для опросника ID: {}", survey.getId());
      }
    } catch (Exception e) {
      log.error("Ошибка при маппинге опросника в DTO: {}", e.getMessage());
    }

    SurveyDto.SurveyDtoBuilder builder = SurveyDto.builder()
        .id(survey.getId())
        .title(survey.getTitle())
        .description(survey.getDescription())
        .status(survey.getStatus() != null ? survey.getStatus().name() : null)
        .startDate(survey.getStartDate())
        .endDate(survey.getEndDate())
        .questions(questionDtos)
        .createdAt(survey.getCreatedAt())
        .updatedAt(survey.getUpdatedAt());

    if (survey.getCreatedBy() != null) {
      builder.user(UserInfoDto.builder()
          .id(survey.getCreatedBy().getId())
          .username(survey.getCreatedBy().getUsername())
          .email(survey.getCreatedBy().getEmail())
          .build());
    }

    return builder.build();
  }

  private QuestionDto mapQuestionToDto(Question question) {
    List<String> options = new ArrayList<>();

    try {
      if (question.getAnswerOptions() != null) {
        options = question.getAnswerOptions().stream()
            .sorted((o1, o2) -> o1.getOrderNumber().compareTo(o2.getOrderNumber()))
            .map(AnswerOption::getText)
            .collect(Collectors.toList());
      } else {
        log.warn("Отсутствуют варианты ответов для вопроса ID: {}", question.getId());
      }
    } catch (Exception e) {
      log.error("Ошибка при маппинге вопроса в DTO: {}", e.getMessage());
    }

    return QuestionDto.builder()
        .id(question.getId())
        .text(question.getText())
        .type(question.getType() != null ? question.getType().name() : null)
        .required(question.isRequired())
        .orderNumber(question.getOrderNumber())
        .options(options)
        .build();
  }

  @Override
  public SurveyDto getSurveyById(Long id) {
    Survey survey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Опросник", "id", id));
    return mapToDto(survey);
  }

  @Override
  public PagedResponseDto<SurveyDto> getAllSurveys(int page, int size, SurveyStatus status) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Survey> surveys;

    if (status != null) {
      surveys = surveyRepository.findByStatus(status, pageable);
    } else {
      surveys = surveyRepository.findAll(pageable);
    }

    List<SurveyDto> surveyDtos = surveys.getContent().stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());

    return PagedResponseDto.<SurveyDto>builder()
        .content(surveyDtos)
        .totalElements(surveys.getTotalElements())
        .totalPages(surveys.getTotalPages())
        .size(surveys.getSize())
        .number(surveys.getNumber())
        .build();
  }

  @Override
  @Transactional
  public SurveyDto updateSurvey(Long id, SurveyDto surveyDto, String userEmail) {
    log.debug("Начало обновления опросника с ID: {}", id);
    validateSurvey(surveyDto);

    Survey survey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Опросник", "id", id));

    User user = userService.findByEmail(userEmail);

    if (!Objects.equals(survey.getCreatedBy().getId(), user.getId())) {
      throw new AccessDeniedException("У вас нет прав для редактирования этого опросника");
    }

    // Обновляем данные опросника
    survey.setTitle(surveyDto.getTitle());
    survey.setDescription(surveyDto.getDescription());
    survey.setStatus(getSurveyStatus(surveyDto));
    survey.setStartDate(surveyDto.getStartDate());
    survey.setEndDate(surveyDto.getEndDate());

    // Получаем текущие вопросы опросника
    List<Question> existingQuestions = questionRepository.findBySurveyOrderByOrderNumberAsc(survey);
    List<Long> existingQuestionIds = existingQuestions.stream()
        .map(Question::getId)
        .collect(Collectors.toList());

    log.debug("Текущие вопросы опросника: {}", existingQuestionIds);

    // Список ID вопросов из запроса (могут быть null для новых вопросов)
    List<Long> updatedQuestionIds = surveyDto.getQuestions().stream()
        .map(QuestionDto::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    log.debug("Вопросы из запроса: {}", updatedQuestionIds);

    // Находим ID вопросов, которые нужно удалить (существующие, но не пришли в
    // запросе)
    List<Long> questionsToDelete = existingQuestionIds.stream()
        .filter(qId -> !updatedQuestionIds.contains(qId))
        .collect(Collectors.toList());

    log.debug("Вопросы для удаления: {}", questionsToDelete);

    // Удаляем вопросы, которых нет в запросе
    if (!questionsToDelete.isEmpty()) {
      for (Long questionId : questionsToDelete) {
        questionRepository.deleteById(questionId);
      }
      entityManager.flush();
      log.debug("Удалены вопросы: {}", questionsToDelete);
    }

    // Обновляем существующие и создаем новые вопросы
    List<Question> updatedQuestions = new ArrayList<>();
    int orderNumber = 1;

    for (QuestionDto questionDto : surveyDto.getQuestions()) {
      try {
        Question question;

        if (questionDto.getId() != null) {
          // Обновляем существующий вопрос
          question = existingQuestions.stream()
              .filter(q -> q.getId().equals(questionDto.getId()))
              .findFirst()
              .orElseThrow(() -> new ResourceNotFoundException("Вопрос", "id", questionDto.getId()));

          question.setText(questionDto.getText());
          question.setType(convertStringToQuestionType(questionDto.getType()));
          question.setRequired(questionDto.getRequired());
          question.setOrderNumber(orderNumber++);

          // Обновляем варианты ответов для существующего вопроса
          updateAnswerOptions(question, questionDto.getOptions());
        } else {
          // Создаем новый вопрос
          question = Question.builder()
              .survey(survey)
              .text(questionDto.getText())
              .type(convertStringToQuestionType(questionDto.getType()))
              .required(questionDto.getRequired())
              .orderNumber(orderNumber++)
              .answerOptions(new ArrayList<>())
              .build();

          // Сохраняем новый вопрос
          question = questionRepository.save(question);

          // Создаем варианты ответов для нового вопроса
          if (questionDto.getOptions() != null && !questionDto.getOptions().isEmpty()) {
            createAnswerOptionsForQuestion(questionDto.getOptions(), question);
          }
        }

        updatedQuestions.add(question);
      } catch (Exception e) {
        log.error("Ошибка при обновлении вопроса: {}", e.getMessage(), e);
        throw e;
      }
    }

    // Обновляем список вопросов в опроснике
    survey.getQuestions().clear();
    survey.getQuestions().addAll(updatedQuestions);

    // Сохраняем обновленный опросник
    Survey updatedSurvey = surveyRepository.save(survey);
    log.debug("Опросник с ID: {} успешно обновлен", id);

    return mapToDto(updatedSurvey);
  }

  /**
   * Обновляет варианты ответов для существующего вопроса
   */
  private void updateAnswerOptions(Question question, List<String> newOptions) {
    // Если нет новых вариантов, очищаем все существующие
    if (newOptions == null || newOptions.isEmpty()) {
      if (question.getAnswerOptions() != null) {
        answerOptionRepository.deleteAll(question.getAnswerOptions());
        question.getAnswerOptions().clear();
      }
      return;
    }

    // Если у вопроса еще нет вариантов ответов, просто создаем новые
    if (question.getAnswerOptions() == null || question.getAnswerOptions().isEmpty()) {
      createAnswerOptionsForQuestion(newOptions, question);
      return;
    }

    // Получаем существующие варианты ответов
    List<AnswerOption> existingOptions = new ArrayList<>(question.getAnswerOptions());

    // Переиспользуем существующие варианты ответов, обновляя их текст
    int orderNumber = 1;
    List<AnswerOption> updatedOptions = new ArrayList<>();

    // Обновляем существующие варианты ответов и создаем новые по необходимости
    for (int i = 0; i < newOptions.size(); i++) {
      String optionText = newOptions.get(i);

      if (i < existingOptions.size()) {
        // Обновляем существующий вариант
        AnswerOption option = existingOptions.get(i);
        option.setText(optionText);
        option.setOrderNumber(orderNumber++);
        updatedOptions.add(option);
      } else {
        // Создаем новый вариант
        AnswerOption option = AnswerOption.builder()
            .question(question)
            .text(optionText)
            .orderNumber(orderNumber++)
            .build();

        option = answerOptionRepository.save(option);
        updatedOptions.add(option);
      }
    }

    // Удаляем лишние варианты ответов
    if (existingOptions.size() > newOptions.size()) {
      List<AnswerOption> optionsToRemove = existingOptions.subList(newOptions.size(), existingOptions.size());
      answerOptionRepository.deleteAll(optionsToRemove);
    }

    // Обновляем список вариантов в вопросе
    question.getAnswerOptions().clear();
    question.getAnswerOptions().addAll(updatedOptions);
  }

  @Override
  @Transactional
  public void deleteSurvey(Long id) {
    log.debug("Начало удаления опросника с ID: {}", id);
    Survey survey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Опросник", "id", id));

    try {
      // Непосредственное удаление опросника
      // Hibernate автоматически удалит зависимые сущности благодаря настройке cascade
      // = CascadeType.ALL
      surveyRepository.delete(survey);
      log.debug("Опросник с ID: {} успешно удален", id);
    } catch (Exception e) {
      log.error("Ошибка при удалении опросника: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional
  public SurveyDto updateSurveyStatus(Long id, SurveyStatusUpdateRequest statusUpdateRequest) {
    log.debug("Начало обновления статуса опросника с ID: {}", id);
    Survey survey = surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Опросник", "id", id));

    SurveyStatus newStatus = statusUpdateRequest.getStatus();
    log.debug("Изменение статуса опросника с '{}' на '{}'", survey.getStatus(), newStatus);

    survey.setStatus(newStatus);
    Survey updatedSurvey = surveyRepository.save(survey);
    log.debug("Статус опросника с ID: {} успешно обновлен", id);

    return mapToDto(updatedSurvey);
  }
}