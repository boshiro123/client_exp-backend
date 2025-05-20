package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.AuthResponse;
import back.client_exp_backend.dto.LoginRequest;
import back.client_exp_backend.dto.LogoutResponse;
import back.client_exp_backend.dto.RegisterRequest;
import back.client_exp_backend.dto.TokenValidationResponse;
import back.client_exp_backend.dto.UserApprovalRequest;
import back.client_exp_backend.dto.UserApprovalResponse;
import back.client_exp_backend.dto.UserDto;
import back.client_exp_backend.exception.ResourceAlreadyExistsException;
import back.client_exp_backend.exception.UnauthorizedException;
import back.client_exp_backend.models.User;
import back.client_exp_backend.models.enums.UserRole;
import back.client_exp_backend.security.JwtTokenProvider;
import back.client_exp_backend.service.AuthService;
import back.client_exp_backend.service.EmailService;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final AuthenticationManager authenticationManager;
  private final EmailService emailService;

  @Override
  public AuthResponse register(RegisterRequest registerRequest) {
    // Проверяем, существует ли пользователь с таким email
    if (userService.existsByEmail(registerRequest.getEmail())) {
      throw new ResourceAlreadyExistsException("Пользователь", "email", registerRequest.getEmail());
    }

    // Создаем нового пользователя с ролью PENDING
    User user = User.builder()
        .username(registerRequest.getUsername())
        .email(registerRequest.getEmail())
        .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
        .role(UserRole.PENDING) // Устанавливаем роль PENDING для всех новых пользователей
        .build();

    // Сохраняем пользователя
    User savedUser = userService.save(user);
    log.info("Зарегистрирован новый пользователь в статусе ожидания: {}", savedUser.getEmail());

    // Отправляем сообщение пользователю о том, что заявка на регистрацию отправлена
    String message = String.format(
        "Уважаемый(ая) %s,\n\n" +
            "Ваша заявка на регистрацию в системе ClientExp успешно создана и находится на рассмотрении. " +
            "Вы получите уведомление, когда администратор одобрит вашу заявку.\n\n" +
            "С уважением, команда ClientExp",
        user.getUsername());

    emailService.sendEmail(user.getEmail(), "Заявка на регистрацию создана", message);

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

      // Проверяем, имеет ли пользователь статус PENDING
      if (user.getRole() == UserRole.PENDING) {
        throw new UnauthorizedException("Ваша учетная запись еще не одобрена администратором");
      }

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

  @Override
  @Transactional
  public UserApprovalResponse approveUser(UserApprovalRequest request) {
    User user = userService.findById(request.getUserId());

    // Проверяем, что пользователь имеет статус PENDING
    if (user.getRole() != UserRole.PENDING) {
      return UserApprovalResponse.builder()
          .message("Пользователь уже обработан")
          .success(false)
          .build();
    }

    // Обновляем роль пользователя на MANAGER
    user = userService.updateUserRole(user.getId(), UserRole.MANAGER);
    log.info("Заявка на регистрацию для пользователя {} одобрена", user.getEmail());

    // Отправляем уведомление пользователю по email
    String message = String.format(
        "Уважаемый(ая) %s,\n\n" +
            "Ваша заявка на регистрацию в системе ClientExp была одобрена. " +
            "Теперь вы можете войти в систему, используя свои учетные данные.\n\n" +
            "%s\n\n" +
            "С уважением, команда ClientExp",
        user.getUsername(),
        request.getMessage() != null ? "Сообщение от администратора: " + request.getMessage() : "");

    emailService.sendEmail(user.getEmail(), "Заявка на регистрацию одобрена", message);

    return UserApprovalResponse.builder()
        .message("Пользователь успешно одобрен")
        .success(true)
        .user(userService.convertToDto(user))
        .build();
  }

  @Override
  @Transactional
  public UserApprovalResponse rejectUser(Long userId) {
    User user = userService.findById(userId);

    // Проверяем, что пользователь имеет статус PENDING
    if (user.getRole() != UserRole.PENDING) {
      return UserApprovalResponse.builder()
          .message("Пользователь уже обработан")
          .success(false)
          .build();
    }

    // Сохраняем данные пользователя перед удалением
    String email = user.getEmail();
    String username = user.getUsername();

    // Удаляем пользователя
    userService.delete(user);
    log.info("Заявка на регистрацию для пользователя {} отклонена", email);

    // Отправляем уведомление пользователю по email
    String message = String.format(
        "Уважаемый(ая) %s,\n\n" +
            "К сожалению, ваша заявка на регистрацию в системе ClientExp была отклонена. " +
            "Если у вас есть вопросы, пожалуйста, свяжитесь с администратором.\n\n" +
            "С уважением, команда ClientExp",
        username);

    emailService.sendEmail(email, "Заявка на регистрацию отклонена", message);

    return UserApprovalResponse.builder()
        .message("Заявка на регистрацию отклонена")
        .success(true)
        .build();
  }
}