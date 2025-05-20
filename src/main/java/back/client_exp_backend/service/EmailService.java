package back.client_exp_backend.service;

public interface EmailService {
  void sendSurveyToAllClients(Long surveyId, String subject, String baseUrl);

  void sendCustomMessageToAllClients(String subject, String messageText);

  void sendEmail(String to, String subject, String text);
}