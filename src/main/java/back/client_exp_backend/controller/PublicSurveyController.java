package back.client_exp_backend.controller;

import back.client_exp_backend.dto.PagedResponseDto;
import back.client_exp_backend.dto.QuestionDto;
import back.client_exp_backend.dto.SurveyDto;
import back.client_exp_backend.models.enums.SurveyStatus;
import back.client_exp_backend.service.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Публичный контроллер для доступа к опросникам без необходимости авторизации
 */
@RestController
@RequestMapping("/api/surveys/public")
@RequiredArgsConstructor
@Slf4j
public class PublicSurveyController {

  private final SurveyService surveyService;

  /**
   * Получение информации об опроснике по ID без авторизации
   * Доступен только для опросников в статусе ACTIVE
   */

  @GetMapping
  public ResponseEntity<PagedResponseDto<SurveyDto>> getAllSurveys(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status) {
    log.info("Запрос на получение списка опросников. Страница: {}, размер: {}, статус: {}", page, size, status);
    SurveyStatus surveyStatus = null;
    if (status != null) {
      try {
        surveyStatus = SurveyStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        log.warn("Неизвестный статус опросника: {}, будут возвращены все опросники", status);
      }
    }
    PagedResponseDto<SurveyDto> response = surveyService.getAllSurveys(page, size, surveyStatus);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.registerModule(new JavaTimeModule());
      log.info("Запрос на получение списка опросников: \n{}", objectMapper.writeValueAsString(response));
    } catch (Exception e) {
      log.error("Ошибка при логировании запроса: {}", e.getMessage());
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SurveyDto> getSurveyById(@PathVariable Long id) {
    log.info("Запрос на получение опросника с id: {}", id);
    try {
      SurveyDto surveyDto = surveyService.getSurveyById(id);

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        log.info("Данные опросника: \n{}", objectMapper.writeValueAsString(surveyDto));
      } catch (Exception e) {
        log.error("Ошибка при логировании ответа: {}", e.getMessage());
      }

      return ResponseEntity.ok(surveyDto);
    } catch (Exception e) {
      log.error("Ошибка при получении опросника: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}