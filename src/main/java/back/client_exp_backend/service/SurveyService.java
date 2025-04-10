package back.client_exp_backend.service;

import back.client_exp_backend.dto.PagedResponseDto;
import back.client_exp_backend.dto.SurveyDto;
import back.client_exp_backend.dto.SurveyStatusUpdateRequest;
import back.client_exp_backend.models.enums.SurveyStatus;

public interface SurveyService {
  SurveyDto createSurvey(SurveyDto surveyDto, String userEmail);

  PagedResponseDto<SurveyDto> getAllSurveys(int page, int size, SurveyStatus status);

  SurveyDto getSurveyById(Long id);

  SurveyDto updateSurvey(Long id, SurveyDto surveyDto, String userEmail);

  void deleteSurvey(Long id);

  SurveyDto updateSurveyStatus(Long id, SurveyStatusUpdateRequest statusUpdateRequest);
}