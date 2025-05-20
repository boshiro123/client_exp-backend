package back.client_exp_backend.service.impl;

import back.client_exp_backend.repository.ClientRepository;
import back.client_exp_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;
  private final ClientRepository clientRepository;

  @Transactional(readOnly = true)
  public void sendSurveyToAllClients(Long surveyId, String subject, String baseUrl) {
    log.info("Начинаем отправку опросника с ID: {} всем клиентам", surveyId);
    int totalSent = 0;

    try {
      var clients = clientRepository.findAll();
      for (var client : clients) {
        try {
          String surveyLink = baseUrl + "/survey/" + surveyId;
          String emailContent = String.format(
              "Уважаемый(ая) %s,\n\n" +
                  "Мы хотели бы узнать Ваше мнение о наших услугах. " +
                  "Пожалуйста, пройдите опрос по следующей ссылке:\n\n" +
                  "%s\n\n" +
                  "Спасибо за Ваше участие!\n" +
                  "С уважением, команда ClientExp",
              client.getName(), surveyLink);

          sendEmail(client.getEmail(), subject, emailContent);
          totalSent++;
        } catch (Exception e) {
          log.error("Ошибка при отправке опросника клиенту {}: {}", client.getEmail(), e.getMessage());
        }
      }
      log.info("Рассылка опросника завершена. Всего отправлено {} писем", totalSent);
    } catch (Exception e) {
      log.error("Ошибка при выполнении рассылки опросника: {}", e.getMessage());
      throw e;
    }
  }

  @Transactional(readOnly = true)
  public void sendCustomMessageToAllClients(String subject, String messageText) {
    log.info("Начинаем отправку тематического письма с темой: {}", subject);
    int totalSent = 0;

    try {
      var clients = clientRepository.findAll();
      for (var client : clients) {
        try {
          String personalizedMessage = String.format(
              "Уважаемый(ая) %s,\n\n" +
                  "%s\n\n" +
                  "С уважением, команда ClientExp",
              client.getName(), messageText);

          sendEmail(client.getEmail(), subject, personalizedMessage);
          totalSent++;
        } catch (Exception e) {
          log.error("Ошибка при отправке письма клиенту {}: {}", client.getEmail(), e.getMessage());
        }
      }
      log.info("Рассылка тематического письма завершена. Всего отправлено {} писем", totalSent);
    } catch (Exception e) {
      log.error("Ошибка при выполнении рассылки тематического письма: {}", e.getMessage());
      throw e;
    }
  }

  public void sendEmail(String to, String subject, String text) {
    log.info("Отправка письма на адрес: {}", to);
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(text);
      mailSender.send(message);
      log.info("Письмо успешно отправлено на адрес: {}", to);
    } catch (Exception e) {
      log.error("Ошибка при отправке письма на адрес {}: {}", to, e.getMessage());
      throw e;
    }
  }
}