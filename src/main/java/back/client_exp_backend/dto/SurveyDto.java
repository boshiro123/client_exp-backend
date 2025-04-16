package back.client_exp_backend.dto;

import back.client_exp_backend.models.enums.SurveyStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyDto {

  private Long id;

  @NotBlank(message = "Название опросника обязательно")
  @Size(min = 3, message = "Название опросника должно содержать минимум 3 символа")
  private String title;

  private String description;

  @NotNull(message = "Статус опросника обязателен")
  private String status;

  private String statusString;

  private String category;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  @Valid
  @Size(min = 3, message = "Опросник должен содержать минимум 3 вопроса")
  private List<QuestionDto> questions;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  private UserInfoDto user;

  @JsonIgnore
  public boolean hasValidStatus() {
    return status != null || statusString != null;
  }
}