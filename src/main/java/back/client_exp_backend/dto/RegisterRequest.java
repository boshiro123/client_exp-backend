package back.client_exp_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  @NotBlank(message = "Имя пользователя обязательно")
  private String username;

  @NotBlank(message = "Email обязателен")
  @Email(message = "Неверный формат email")
  private String email;

  @NotBlank(message = "Пароль обязателен")
  @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
  private String password;

  @NotBlank(message = "Роль обязательна")
  private String role;
}