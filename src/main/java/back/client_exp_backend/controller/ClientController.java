package back.client_exp_backend.controller;

import back.client_exp_backend.dto.ClientWithAnswersDto;
import back.client_exp_backend.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

  private final ClientService clientService;

  @GetMapping("/with-answers")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<List<ClientWithAnswersDto>> getAllClientsWithAnswers() {
    log.info("Получение всех клиентов с их ответами на вопросы");
    List<ClientWithAnswersDto> clients = clientService.getAllClientsWithAnswers();
    return ResponseEntity.ok(clients);
  }
}