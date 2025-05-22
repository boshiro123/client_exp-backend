package back.client_exp_backend.service;

import back.client_exp_backend.dto.ClientWithAnswersDto;
import java.util.List;

public interface ClientService {
  List<ClientWithAnswersDto> getAllClientsWithAnswers();
}