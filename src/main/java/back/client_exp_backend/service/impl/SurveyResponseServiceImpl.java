package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.RespondentDto;
import back.client_exp_backend.dto.SurveyAnswerDto;
import back.client_exp_backend.dto.SurveyResponseDto;
import back.client_exp_backend.dto.SurveyResponseResultDto;
import back.client_exp_backend.exception.ResourceNotFoundException;
import back.client_exp_backend.models.*;
import back.client_exp_backend.models.enums.QuestionType;
import back.client_exp_backend.repository.AnswerOptionRepository;
import back.client_exp_backend.repository.ClientAnswerRepository;
import back.client_exp_backend.repository.ClientRepository;
import back.client_exp_backend.repository.QuestionRepository;
import back.client_exp_backend.repository.SurveyRepository;
import back.client_exp_backend.service.SurveyResponseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyResponseServiceImpl implements SurveyResponseService {

  private final SurveyRepository surveyRepository;
  private final QuestionRepository questionRepository;
  private final ClientRepository clientRepository;
  private final ClientAnswerRepository clientAnswerRepository;
  private final AnswerOptionRepository answerOptionRepository;

  @Override
  @Transactional
  public SurveyResponseResultDto processSurveyResponse(SurveyResponseDto responseDto) {
    log.info("Обработка ответов на опрос surveyId={}", responseDto.getSurveyId());

    // Находим опрос
    Survey survey = surveyRepository.findById(responseDto.getSurveyId())
        .orElseThrow(() -> new ResourceNotFoundException("Опрос с ID " + responseDto.getSurveyId() + " не найден"));

    // Находим или создаем клиента
    Client client = findOrCreateClient(responseDto.getRespondent());

    // Загружаем все вопросы опроса для быстрого доступа
    List<Question> questions = questionRepository.findBySurveyOrderByOrderNumberAsc(survey);
    Map<Long, Question> questionMap = new HashMap<>();
    for (Question question : questions) {
      questionMap.put(question.getId(), question);
    }

    // Загружаем все варианты ответов для быстрого доступа
    Map<Long, Map<String, AnswerOption>> answerOptionsMap = new HashMap<>();
    for (Question question : questions) {
      Map<String, AnswerOption> optionsMap = new HashMap<>();
      if (question.getAnswerOptions() != null) {
        for (AnswerOption option : question.getAnswerOptions()) {
          optionsMap.put(option.getText(), option);
        }
      }
      answerOptionsMap.put(question.getId(), optionsMap);
    }

    // Обрабатываем ответы
    List<ClientAnswer> savedAnswers = new ArrayList<>();
    for (SurveyAnswerDto answerDto : responseDto.getAnswers()) {
      Long questionId = answerDto.getQuestionId();

      // Проверяем, что вопрос существует
      if (!questionMap.containsKey(questionId)) {
        log.warn("Вопрос с ID {} не найден, пропускаем ответ", questionId);
        continue;
      }

      Question question = questionMap.get(questionId);

      // Создаем объект ответа клиента
      ClientAnswer clientAnswer = null;

      if (question.getType() == QuestionType.SINGLE_CHOICE) {
        // Для вопросов с одним вариантом ответа
        String answerText = answerDto.getTextAnswer();
        if (answerText != null && !answerText.isBlank()) {
          Map<String, AnswerOption> options = answerOptionsMap.get(questionId);
          AnswerOption selectedOption = options.get(answerText);

          clientAnswer = ClientAnswer.builder()
              .client(client)
              .survey(survey)
              .question(question)
              .answerOption(selectedOption)
              .textAnswer(answerText)
              .build();
        }
      } else if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
        // Для вопросов с множественным выбором
        List<String> answers = answerDto.getAnswerList();
        if (answers != null && !answers.isEmpty()) {
          // Берем первый вариант для сохранения в БД, остальные можно добавить как
          // дополнительные записи
          String firstAnswer = answers.get(0);
          Map<String, AnswerOption> options = answerOptionsMap.get(questionId);
          AnswerOption selectedOption = options.get(firstAnswer);

          clientAnswer = ClientAnswer.builder()
              .client(client)
              .survey(survey)
              .question(question)
              .answerOption(selectedOption)
              .textAnswer(String.join(", ", answers))
              .build();
        }
      } else if (question.getType() == QuestionType.TEXT) {
        // Для текстовых вопросов
        String answerText = answerDto.getTextAnswer();
        if (answerText != null) {
          clientAnswer = ClientAnswer.builder()
              .client(client)
              .survey(survey)
              .question(question)
              .textAnswer(answerText)
              .build();
        }
      } else if (question.getType() == QuestionType.RATING) {
        // Для вопросов с рейтингом
        Integer numericAnswer = answerDto.getNumericAnswer();
        if (numericAnswer != null) {
          clientAnswer = ClientAnswer.builder()
              .client(client)
              .survey(survey)
              .question(question)
              .numericAnswer(numericAnswer)
              .build();
        } else {
          // Если рейтинг передан как текст
          String answerText = answerDto.getTextAnswer();
          clientAnswer = ClientAnswer.builder()
              .client(client)
              .survey(survey)
              .question(question)
              .textAnswer(answerText)
              .build();
        }
      }

      if (clientAnswer != null) {
        // Сохраняем ответ
        ClientAnswer savedAnswer = clientAnswerRepository.save(clientAnswer);
        savedAnswers.add(savedAnswer);
      }
    }

    // Создаем результат
    return SurveyResponseResultDto.builder()
        .id(client.getId())
        .surveyId(survey.getId())
        .surveyTitle(survey.getTitle())
        .respondentName(client.getName())
        .respondentEmail(client.getEmail())
        .answersCount(savedAnswers.size())
        .submittedAt(LocalDateTime.now())
        .message("Ответы успешно сохранены")
        .build();
  }

  /**
   * Находит или создает клиента на основе данных респондента
   */
  private Client findOrCreateClient(RespondentDto respondentDto) {
    // Проверяем, существует ли клиент с таким email
    Optional<Client> existingClient = clientRepository.findByEmail(respondentDto.getEmail());

    if (existingClient.isPresent()) {
      log.info("Найден существующий клиент: {}", respondentDto.getEmail());
      Client client = existingClient.get();

      // Обновляем имя клиента, если оно изменилось
      if (!client.getName().equals(respondentDto.getName())) {
        client.setName(respondentDto.getName());
        client = clientRepository.save(client);
      }

      return client;
    } else {
      log.info("Создание нового клиента: {}", respondentDto.getEmail());

      // Создаем нового клиента
      Client newClient = Client.builder()
          .name(respondentDto.getName())
          .email(respondentDto.getEmail())
          .build();

      return clientRepository.save(newClient);
    }
  }

  @Override
  public List<SurveyResponseDto> getFormattedResponsesBySurveyId(Long surveyId) {
    log.info("Получение форматированных ответов на опрос с ID={}", surveyId);

    // Находим опрос
    Survey survey = surveyRepository.findById(surveyId)
        .orElseThrow(() -> new ResourceNotFoundException("Опрос с ID " + surveyId + " не найден"));

    // Получаем список всех ответов на данный опрос
    List<ClientAnswer> allAnswers = clientAnswerRepository.findBySurvey(survey);

    if (allAnswers.isEmpty()) {
      return new ArrayList<>();
    }

    // Группируем ответы по клиентам
    Map<Client, List<ClientAnswer>> answersByClient = allAnswers.stream()
        .collect(Collectors.groupingBy(ClientAnswer::getClient));

    // Формируем список ответов в требуемом формате
    List<SurveyResponseDto> formattedResponses = new ArrayList<>();

    answersByClient.forEach((client, clientAnswers) -> {
      // Создаем объект ответа на опрос
      SurveyResponseDto responseDto = new SurveyResponseDto();
      responseDto.setId(client.getId());
      responseDto.setSurveyId(surveyId);

      // Создаем и заполняем информацию о респонденте
      RespondentDto respondentDto = new RespondentDto();
      respondentDto.setName(client.getName());
      respondentDto.setEmail(client.getEmail());
      respondentDto.setConsent(true); // Предполагаем, что если клиент ответил, то согласие было дано
      responseDto.setRespondent(respondentDto);

      // Формируем список ответов
      List<SurveyAnswerDto> answers = clientAnswers.stream()
          .map(this::createSurveyAnswerFromClientAnswer)
          .collect(Collectors.toList());

      responseDto.setAnswers(answers);

      // Устанавливаем даты создания и обновления
      LocalDateTime createdAt = clientAnswers.stream()
          .map(ClientAnswer::getCreatedAt)
          .min(LocalDateTime::compareTo)
          .orElse(LocalDateTime.now());

      // Для даты обновления используем ту же дату создания, так как в модели
      // ClientAnswer
      // нет поля updatedAt
      LocalDateTime updatedAt = createdAt;

      responseDto.setCreatedAt(createdAt);
      responseDto.setUpdatedAt(updatedAt);

      formattedResponses.add(responseDto);
    });

    return formattedResponses;
  }

  /**
   * Создает объект SurveyAnswerDto из объекта ClientAnswer
   * 
   * @param clientAnswer ответ клиента из БД
   * @return объект SurveyAnswerDto с отформатированным ответом
   */
  private SurveyAnswerDto createSurveyAnswerFromClientAnswer(ClientAnswer clientAnswer) {
    SurveyAnswerDto answerDto = new SurveyAnswerDto();
    answerDto.setQuestionId(clientAnswer.getQuestion().getId());

    // Определяем тип вопроса и форматируем ответ соответственно
    QuestionType questionType = clientAnswer.getQuestion().getType();

    // Получаем текст ответа в зависимости от его типа
    String answerText = null;
    if (clientAnswer.getTextAnswer() != null) {
      answerText = clientAnswer.getTextAnswer();
    } else if (clientAnswer.getNumericAnswer() != null) {
      answerText = clientAnswer.getNumericAnswer().toString();
    } else if (clientAnswer.getAnswerOption() != null) {
      answerText = clientAnswer.getAnswerOption().getText();
    }

    if (answerText == null) {
      answerText = "";
    }

    if (questionType == QuestionType.MULTIPLE_CHOICE && answerText.contains(",")) {
      // Для вопросов с множественным выбором, разделяем ответы
      List<String> answerList = List.of(answerText.split(",\\s*"));
      answerDto.setAnswer(answerList);
    } else if (questionType == QuestionType.RATING && answerText.matches("\\d+")) {
      // Для рейтинговых вопросов преобразуем ответ в число
      try {
        answerDto.setAnswer(Integer.parseInt(answerText));
      } catch (NumberFormatException e) {
        answerDto.setAnswer(answerText);
      }
    } else {
      // Для остальных типов вопросов оставляем ответ как строку
      answerDto.setAnswer(answerText);
    }

    return answerDto;
  }
}