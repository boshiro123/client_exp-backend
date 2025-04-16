package back.client_exp_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyResponseResultDto {
  private Long id;
  private Long surveyId;
  private String surveyTitle;
  private String respondentName;
  private String respondentEmail;
  private Integer answersCount;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime submittedAt;
  private String message;

  private Long uniqueRespondentsCount;
  private Long totalAnswersCount;
  private List<ClientAnswerDto> answers;
}