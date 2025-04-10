package back.client_exp_backend.dto;

import back.client_exp_backend.models.enums.SurveyStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyStatusUpdateRequest {

  @NotNull(message = "Статус опросника обязателен")
  private SurveyStatus status;

  private String statusString;

  @JsonIgnore
  public boolean hasValidStatus() {
    return status != null || statusString != null;
  }
}