package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.AuthResponse;
import back.client_exp_backend.dto.LoginRequest;
import back.client_exp_backend.dto.LogoutResponse;
import back.client_exp_backend.dto.RegisterRequest;
import back.client_exp_backend.dto.TokenValidationResponse;
import back.client_exp_backend.dto.UserDto;
import back.client_exp_backend.exception.ResourceAlreadyExistsException;
import back.client_exp_backend.models.User;
import back.client_exp_backend.models.enums.UserRole;
import back.client_exp_backend.security.JwtTokenProvider;
import back.client_exp_backend.service.AuthService;
import back.client_exp_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
    log.info("Зарегистрирован новый пользователь: {}", savedUser.getEmail());

    // Создаем JWT токен
    String token = tokenProvider.generateToken(savedUser);

    // Преобразуем пользователя в DTO
    UserDto userDto = userService.convertToDto(savedUser);

    // Возвращаем ответ
    return new AuthResponse(token, userDto);
  }

  @Override
  public AuthResponse login(LoginRequest loginRequest) {
    try {
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

      log.info("Пользователь успешно вошел в систему: {}", user.getEmail());

      // Преобразуем пользователя в DTO
      UserDto userDto = userService.convertToDto(user);

      // Возвращаем ответ
      return new AuthResponse(token, userDto);
    } catch (BadCredentialsException e) {
      log.warn("Неудачная попытка входа для email: {}", loginRequest.getEmail());
      throw e;
    }
  }

  @Override
  public TokenValidationResponse validateToken(String token) {
    if (!tokenProvider.validateToken(token)) {
      return TokenValidationResponse.builder()
          .valid(false)
          .build();
    }

    // Получаем email пользователя из токена
    String email = tokenProvider.getEmailFromToken(token);

    // Находим пользователя по email
    User user = userService.findByEmail(email);

    // Преобразуем пользователя в DTO
    UserDto userDto = userService.convertToDto(user);

    return TokenValidationResponse.builder()
        .valid(true)
        .user(userDto)
        .build();
  }

  @Override
  public LogoutResponse logout(String token) {
    // Добавляем токен в черный список
    tokenProvider.blacklistToken(token);

    // Получаем email пользователя из токена
    String email = tokenProvider.getEmailFromToken(token);
    log.info("Пользователь вышел из системы: {}", email);

    return new LogoutResponse("Выход выполнен успешно");
  }
}