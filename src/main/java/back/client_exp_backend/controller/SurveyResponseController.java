package back.client_exp_backend.controller;

import back.client_exp_backend.dto.SurveyResponseDto;
import back.client_exp_backend.dto.SurveyResponseResultDto;
import back.client_exp_backend.exception.ApiError;
import back.client_exp_backend.models.ClientAnswer;
import back.client_exp_backend.repository.ClientAnswerRepository;
import back.client_exp_backend.repository.SurveyRepository;
import back.client_exp_backend.service.SurveyResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surveys/responses")
@RequiredArgsConstructor
@Slf4j
public class SurveyResponseController {

  private final SurveyResponseService surveyResponseService;
  private final ClientAnswerRepository clientAnswerRepository;
  private final SurveyRepository surveyRepository;

  @PostMapping
  public ResponseEntity<Object> submitSurveyResponse(@Valid @RequestBody SurveyResponseDto responseDto) {
    try {
      // Логирование запроса
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.registerModule(new JavaTimeModule());
      log.info("Получены ответы на опрос: \n{}", objectMapper.writeValueAsString(responseDto));

      // Проверка наличия данных о респонденте
      if (responseDto.getRespondent() == null) {
        log.warn("Отсутствуют данные о респонденте");
        return ResponseEntity.badRequest().body(
            new ApiError(HttpStatus.BAD_REQUEST, "Ошибка валидации", "Отсутствуют данные о респонденте"));
      }

      // Обработка ответов на опрос
      SurveyResponseResultDto result = surveyResponseService.processSurveyResponse(responseDto);

      // Логирование результата
      log.info("Ответы на опрос успешно сохранены. ID клиента: {}, ID опроса: {}, количество ответов: {}",
          result.getId(), result.getSurveyId(), result.getAnswersCount());

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Ошибка при обработке ответов на опрос", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", e.getMessage()));
    }
  }

  @GetMapping("/survey/{surveyId}/summary")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<Object> getSurveyResponsesSummary(@PathVariable Long surveyId) {
    try {
      // Проверяем существование опроса
      boolean surveyExists = surveyRepository.existsById(surveyId);
      if (!surveyExists) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(HttpStatus.NOT_FOUND, "Опрос не найден", "Опрос с ID " + surveyId + " не найден"));
      }

      // Получаем количество уникальных респондентов
      List<Long> respondentIds = clientAnswerRepository.findDistinctClientIdsBySurveyId(surveyId);
      int respondentsCount = respondentIds.size();

      // Получаем общее количество ответов
      List<ClientAnswer> answers = clientAnswerRepository.findBySurvey(
          surveyRepository.findById(surveyId).get());
      int totalAnswersCount = answers.size();

      // Формируем результат
      Map<String, Object> result = new HashMap<>();
      result.put("surveyId", surveyId);
      result.put("respondentsCount", respondentsCount);
      result.put("totalAnswersCount", totalAnswersCount);
      result.put("respondentIds", respondentIds);

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Ошибка при получении сводки ответов по опросу с ID {}", surveyId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", e.getMessage()));
    }
  }

  // @GetMapping("/{surveyId}")
  // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  // public ResponseEntity<Object> getClientSurveyResponses(
  // @PathVariable Long surveyId) {
  // try {
  // // Проверяем существование опроса и клиента
  // boolean surveyExists = surveyRepository.existsById(surveyId);
  // if (!surveyExists) {
  // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
  // new ApiError(HttpStatus.NOT_FOUND, "Опрос не найден", "Опрос с ID " +
  // surveyId + " не найден"));
  // }

  // // Получаем ответы клиента на опрос
  // List<ClientAnswer> clientAnswers = clientAnswerRepository.findBySurvey(
  // surveyRepository.findById(surveyId).get());
  // if (clientAnswers.isEmpty()) {
  // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
  // new ApiError(HttpStatus.NOT_FOUND, "Ответы не найдены",
  // "Ответы клиента с ID " + " на опрос с ID " + surveyId + " не найдены"));
  // }

  // // Формируем результат
  // Map<String, Object> result = new HashMap<>();
  // // result.put("clientId", clientId);
  // result.put("surveyId", surveyId);
  // result.put("answersCount", clientAnswers.size());
  // result.put("answers", clientAnswers);

  // return ResponseEntity.ok(result);
  // } catch (Exception e) {
  // log.error("Ошибка при получении ответов на опрос {}", surveyId, e);
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
  // new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера",
  // e.getMessage()));
  // }
  // }

  @GetMapping("/{surveyId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<Object> getFormattedResponses(@PathVariable Long surveyId) {
    try {
      // Проверяем существование опроса
      boolean surveyExists = surveyRepository.existsById(surveyId);
      if (!surveyExists) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(HttpStatus.NOT_FOUND, "Опрос не найден", "Опрос с ID " + surveyId + " не найден"));
      }

      // Получаем отформатированные ответы
      List<SurveyResponseDto> formattedResponses = surveyResponseService.getFormattedResponsesBySurveyId(surveyId);

      if (formattedResponses.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(HttpStatus.NOT_FOUND, "Ответы не найдены",
                "Ответы на опрос с ID " + surveyId + " не найдены"));
      }

      return ResponseEntity.ok(formattedResponses);
    } catch (Exception e) {
      log.error("Ошибка при получении отформатированных ответов на опрос {}", surveyId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", e.getMessage()));
    }
  }
}