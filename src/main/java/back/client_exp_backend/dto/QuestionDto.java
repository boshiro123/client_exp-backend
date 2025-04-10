package back.client_exp_backend.dto;

import back.client_exp_backend.models.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDto {

  private Long id;

  @NotBlank(message = "Текст вопроса обязателен")
  private String text;

  @NotNull(message = "Тип вопроса обязателен")
  private String type;

  @NotNull(message = "Поле 'обязательность' должно быть указано")
  private Boolean required;

  private String category;

  private Integer orderNumber;

  @Size(min = 1, message = "Для вопросов с выбором ответа должен быть хотя бы один вариант ответа")
  private List<String> options;

  @JsonIgnore
  public boolean hasValidType() {
    return type != null;
  }
}