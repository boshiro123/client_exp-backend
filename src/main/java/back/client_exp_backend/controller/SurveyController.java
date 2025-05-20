package back.client_exp_backend.controller;

import back.client_exp_backend.dto.PagedResponseDto;
import back.client_exp_backend.dto.QuestionDto;
import back.client_exp_backend.dto.SurveyDto;
import back.client_exp_backend.dto.SurveyStatusUpdateRequest;
import back.client_exp_backend.models.enums.SurveyStatus;
import back.client_exp_backend.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Slf4j
public class SurveyController {

  private final SurveyService surveyService;

  @PostMapping
  public ResponseEntity<SurveyDto> createSurvey(
      @Valid @RequestBody SurveyDto surveyDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.registerModule(new JavaTimeModule());
      log.info("Запрос на создание опросника: \n{}", objectMapper.writeValueAsString(surveyDto));
    } catch (Exception e) {
      log.error("Ошибка при логировании запроса: {}", e.getMessage());
    }

    try {
      // Преобразование типов вопросов
      if (surveyDto.getQuestions() != null) {
        for (QuestionDto questionDto : surveyDto.getQuestions()) {
          if (questionDto.getType() == null) {
            log.warn("Тип вопроса не указан");
            return ResponseEntity.badRequest().body(null);
          }
        }
      }

      log.info("Создание опросника от пользователя: {}", "manager@mail.ru");
      SurveyDto createdSurvey = surveyService.createSurvey(surveyDto, "manager@mail.ru");
      return new ResponseEntity<>(createdSurvey, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      log.warn("Ошибка валидации при создании опросника: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    } catch (Exception e) {
      log.error("Ошибка при создании опросника: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

  }

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

  @PutMapping("/{id}")
  public ResponseEntity<SurveyDto> updateSurvey(
      @PathVariable Long id,
      @Valid @RequestBody SurveyDto surveyDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("Запрос на обновление опросника с id: {}", id);

    try {
      SurveyDto updatedSurvey = surveyService.updateSurvey(id, surveyDto, surveyDto.getUser().getEmail());
      return ResponseEntity.ok(updatedSurvey);
    } catch (IllegalArgumentException e) {
      log.warn("Ошибка валидации при обновлении опросника: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    } catch (Exception e) {
      log.error("Ошибка при обновлении опросника: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSurvey(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("Запрос на удаление опросника с id: {}", id);
    try {
      surveyService.deleteSurvey(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Ошибка при удалении опросника: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<SurveyDto> updateSurveyStatus(
      @PathVariable Long id,
      @Valid @RequestBody SurveyStatusUpdateRequest statusUpdateRequest) {
    log.info("Запрос на изменение статуса опросника с id: {} на {}", id,
        statusUpdateRequest.getStatus() != null ? statusUpdateRequest.getStatus()
            : statusUpdateRequest.getStatusString());

    try {
      SurveyDto updatedSurvey = surveyService.updateSurveyStatus(id, statusUpdateRequest);
      return ResponseEntity.ok(updatedSurvey);
    } catch (IllegalArgumentException e) {
      log.warn("Ошибка валидации при изменении статуса опросника: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    } catch (Exception e) {
      log.error("Ошибка при изменении статуса опросника: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  private SurveyStatus normalizeStatus(String status) {
    if (status == null) {
      return null;
    }

    status = status.toUpperCase().trim();

    // Обработка русских названий статусов
    if ("АКТИВНЫЙ".equals(status) || "АКТИВЕН".equals(status)) {
      return SurveyStatus.ACTIVE;
    } else if ("ЧЕРНОВИК".equals(status)) {
      return SurveyStatus.DRAFT;
    } else if ("ЗАВЕРШЕННЫЙ".equals(status) || "ЗАВЕРШЕН".equals(status)) {
      return SurveyStatus.COMPLETED;
    }

    // Обработка строковых представлений из фронтенда
    if ("ACTIVE".equals(status)) {
      return SurveyStatus.ACTIVE;
    } else if ("DRAFT".equals(status)) {
      return SurveyStatus.DRAFT;
    } else if ("COMPLETED".equals(status)) {
      return SurveyStatus.COMPLETED;
    }

    try {
      return SurveyStatus.valueOf(status);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Неизвестный статус опросника: " + status);
    }
  }

  // /**
  // * Преобразует строковое представление типа вопроса в enum QuestionType
  // */`
  // private QuestionType normalizeQuestionType(String type) {
  // if (type == null) {
  // return null;
  // }

  // type = type.toUpperCase().replace("_", "").trim();

  // if ("SINGLECHOICE".equals(type) || "SINGLE_CHOICE".equals(type) ||
  // "SINGLE".equals(type)) {
  // return QuestionType.SINGLE_CHOICE;
  // } else if ("MULTIPLECHOICE".equals(type) || "MULTIPLE_CHOICE".equals(type) ||
  // "MULTIPLE".equals(type)) {
  // return QuestionType.MULTIPLE_CHOICE;
  // } else if ("TEXT".equals(type) || "ТЕКСТ".equals(type)) {
  // return QuestionType.TEXT;
  // } else if ("RATING".equals(type) || "РЕЙТИНГ".equals(type)) {
  // return QuestionType.RATING;
  // }

  // try {
  // return QuestionType.valueOf(type);
  // } catch (IllegalArgumentException e) {
  // throw new IllegalArgumentException("Неизвестный тип вопроса: " + type);
  // }
  // }
}