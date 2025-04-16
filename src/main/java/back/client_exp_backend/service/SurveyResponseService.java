package back.client_exp_backend.service;

import back.client_exp_backend.dto.SurveyResponseDto;
import back.client_exp_backend.dto.SurveyResponseResultDto;

import java.util.List;

public interface SurveyResponseService {
  SurveyResponseResultDto processSurveyResponse(SurveyResponseDto responseDto);

  List<SurveyResponseDto> getFormattedResponsesBySurveyId(Long surveyId);
}