package back.client_exp_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RespondentDto {

  @NotBlank(message = "Имя респондента обязательно")
  private String name;

  @NotBlank(message = "Email обязателен")
  @Email(message = "Некорректный формат email")
  private String email;

  @NotNull(message = "Согласие на обработку данных обязательно")
  private Boolean consent;
}