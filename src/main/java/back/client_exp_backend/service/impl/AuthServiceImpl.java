package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.AuthResponse;
import back.client_exp_backend.dto.LoginRequest;
import back.client_exp_backend.dto.RegisterRequest;
import back.client_exp_backend.dto.UserDto;
import back.client_exp_backend.exception.ResourceAlreadyExistsException;
import back.client_exp_backend.models.User;
import back.client_exp_backend.models.enums.UserRole;
import back.client_exp_backend.security.JwtTokenProvider;
import back.client_exp_backend.service.AuthService;
import back.client_exp_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final AuthenticationManager authenticationManager;

  @Override
  public AuthResponse register(RegisterRequest registerRequest) {
    // Проверяем, существует ли пользователь с таким email
    if (userService.existsByEmail(registerRequest.getEmail())) {
      throw new ResourceAlreadyExistsException("Пользователь", "email", registerRequest.getEmail());
    }

    // Создаем нового пользователя
    User user = User.builder()
        .username(registerRequest.getUsername())
        .email(registerRequest.getEmail())
        .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
        .role(UserRole.valueOf(registerRequest.getRole().toUpperCase()))
        .build();

    // Сохраняем пользователя
    User savedUser = userService.save(user);

    // Создаем JWT токен
    String token = tokenProvider.generateToken(savedUser);

    // Преобразуем пользователя в DTO
    UserDto userDto = userService.convertToDto(savedUser);

    // Возвращаем ответ
    return new AuthResponse(token, userDto);
  }

  @Override
  public AuthResponse login(LoginRequest loginRequest) {
    // Аутентифицируем пользователя
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword()));

    // Устанавливаем аутентификацию в контекст безопасности
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // Находим пользователя
    User user = userService.findByEmail(loginRequest.getEmail());

    // Создаем JWT токен
    String token = tokenProvider.generateToken(user);

    // Преобразуем пользователя в DTO
    UserDto userDto = userService.convertToDto(user);

    // Возвращаем ответ
    return new AuthResponse(token, userDto);
  }
}