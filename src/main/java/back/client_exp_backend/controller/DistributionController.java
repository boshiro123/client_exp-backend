package back.client_exp_backend.controller;

import back.client_exp_backend.dto.DistributionResponse;
import back.client_exp_backend.dto.MessageDistributionRequest;
import back.client_exp_backend.dto.SurveyDistributionRequest;
import back.client_exp_backend.repository.ClientRepository;
import back.client_exp_backend.repository.SurveyRepository;
import back.client_exp_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/distribution")
@RequiredArgsConstructor
public class DistributionController {

  private final EmailService emailService;
  private final ClientRepository clientRepository;
  private final SurveyRepository surveyRepository;

  @PostMapping("/survey")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<DistributionResponse> distributeSurvey(@RequestBody SurveyDistributionRequest request) {
    log.info("Получен запрос на рассылку опросника с ID: {}", request.getSurveyId());

    // Проверка существования опросника
    if (!surveyRepository.existsById(request.getSurveyId())) {
      return ResponseEntity.badRequest().body(
          DistributionResponse.builder()
              .message("Опросник с ID " + request.getSurveyId() + " не найден")
              .success(false)
              .totalRecipients(0)
              .build());
    }

    int totalClients = (int) clientRepository.count();

    if (totalClients == 0) {
      return ResponseEntity.ok(
          DistributionResponse.builder()
              .message("В системе нет клиентов для рассылки")
              .success(false)
              .totalRecipients(0)
              .build());
    }

    // Базовый URL для формирования ссылки на опросник
    String baseUrl = "http://localhost:3000";

    // Отправка опросника всем клиентам
    emailService.sendSurveyToAllClients(
        request.getSurveyId(),
        request.getSubject() != null ? request.getSubject() : "Приглашение пройти опрос",
        baseUrl);

    return ResponseEntity.ok(
        DistributionResponse.builder()
            .message("Опросник успешно отправлен всем клиентам")
            .success(true)
            .totalRecipients(totalClients)
            .build());
  }

  @PostMapping("/message")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<DistributionResponse> distributeMessage(@RequestBody MessageDistributionRequest request) {
    log.info("Получен запрос на рассылку тематического письма");

    if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
      return ResponseEntity.badRequest().body(
          DistributionResponse.builder()
              .message("Сообщение не может быть пустым")
              .success(false)
              .totalRecipients(0)
              .build());
    }

    int totalClients = (int) clientRepository.count();

    if (totalClients == 0) {
      return ResponseEntity.ok(
          DistributionResponse.builder()
              .message("В системе нет клиентов для рассылки")
              .success(false)
              .totalRecipients(0)
              .build());
    }

    // Отправка сообщения всем клиентам
    emailService.sendCustomMessageToAllClients(
        request.getSubject() != null ? request.getSubject() : "Информация от ClientExp",
        request.getMessage());

    return ResponseEntity.ok(
        DistributionResponse.builder()
            .message("Тематическое письмо успешно отправлено всем клиентам")
            .success(true)
            .totalRecipients(totalClients)
            .build());
  }
}