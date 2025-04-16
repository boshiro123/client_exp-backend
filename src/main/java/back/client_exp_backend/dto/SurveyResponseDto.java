package back.client_exp_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class SurveyResponseDto {

  private Long id;

  @NotNull(message = "ID опроса обязателен")
  private Long surveyId;

  @Valid
  @NotNull(message = "Информация о респонденте обязательна")
  private RespondentDto respondent;

  @Valid
  @NotEmpty(message = "Список ответов не может быть пустым")
  private List<SurveyAnswerDto> answers;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private LocalDateTime updatedAt;
}