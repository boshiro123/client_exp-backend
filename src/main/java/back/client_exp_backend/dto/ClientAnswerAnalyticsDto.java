package back.client_exp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientAnswerAnalyticsDto {
  private Long questionId;
  private String questionText;
  private Long totalResponses;
  private Map<String, Long> answerDistribution;
  private List<ClientAnswerDto> clientAnswers;
}