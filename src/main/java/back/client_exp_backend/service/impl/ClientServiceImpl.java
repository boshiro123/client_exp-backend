package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.ClientAnswerDto;
import back.client_exp_backend.dto.ClientWithAnswersDto;
import back.client_exp_backend.models.Client;
import back.client_exp_backend.models.ClientAnswer;
import back.client_exp_backend.repository.ClientAnswerRepository;
import back.client_exp_backend.repository.ClientRepository;
import back.client_exp_backend.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

  private final ClientRepository clientRepository;
  private final ClientAnswerRepository clientAnswerRepository;

  @Override
  @Transactional(readOnly = true)
  public List<ClientWithAnswersDto> getAllClientsWithAnswers() {
    List<Client> clients = clientRepository.findAll();

    return clients.stream()
        .map(this::mapToClientWithAnswersDto)
        .collect(Collectors.toList());
  }

  private ClientWithAnswersDto mapToClientWithAnswersDto(Client client) {
    List<ClientAnswer> clientAnswers = clientAnswerRepository.findByClient(client);

    List<ClientAnswerDto> answerDtos = clientAnswers.stream()
        .map(this::mapToClientAnswerDto)
        .collect(Collectors.toList());

    return ClientWithAnswersDto.builder()
        .id(client.getId())
        .name(client.getName())
        .email(client.getEmail())
        .phone(client.getPhone())
        .ageGroup(client.getAgeGroup())
        .gender(client.getGender())
        .profession(client.getProfession())
        .region(client.getRegion())
        .locationPreference(client.getLocationPreference())
        .usageFrequency(client.getUsageFrequency())
        .source(client.getSource())
        .socialNetwork(client.getSocialNetwork())
        .usagePurpose(client.getUsagePurpose())
        .clientSince(client.getClientSince())
        .createdAt(client.getCreatedAt())
        .updatedAt(client.getUpdatedAt())
        .answers(answerDtos)
        .build();
  }

  private ClientAnswerDto mapToClientAnswerDto(ClientAnswer clientAnswer) {
    String answer = clientAnswer.getAnswerOption() != null
        ? clientAnswer.getAnswerOption().getText()
        : clientAnswer.getTextAnswer() != null
            ? clientAnswer.getTextAnswer()
            : clientAnswer.getNumericAnswer() != null
                ? clientAnswer.getNumericAnswer().toString()
                : null;

    return ClientAnswerDto.builder()
        .id(clientAnswer.getId())
        .questionId(clientAnswer.getQuestion().getId())
        .questionText(clientAnswer.getQuestion().getText())
        .answer(answer)
        .clientId(clientAnswer.getClient().getId())
        .clientName(clientAnswer.getClient().getName())
        .answeredAt(clientAnswer.getCreatedAt())
        .build();
  }
}