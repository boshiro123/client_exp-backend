package back.client_exp_backend.controller;

import back.client_exp_backend.dto.AuthResponse;
import back.client_exp_backend.dto.LoginRequest;
import back.client_exp_backend.dto.LogoutResponse;
import back.client_exp_backend.dto.RegisterRequest;
import back.client_exp_backend.dto.TokenValidationResponse;
import back.client_exp_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
    return ResponseEntity.ok(authService.register(registerRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.login(loginRequest));
  }

  @GetMapping("/validate")
  public ResponseEntity<TokenValidationResponse> validateToken(
      @RequestHeader("Authorization") String authHeader) {
    String token = extractTokenFromHeader(authHeader);
    return ResponseEntity.ok(authService.validateToken(token));
  }

  @PostMapping("/logout")
  public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
    String token = extractTokenFromHeader(authHeader);
    return ResponseEntity.ok(authService.logout(token));
  }

  private String extractTokenFromHeader(String authHeader) {
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    throw new IllegalArgumentException("Токен авторизации отсутствует или имеет неверный формат");
  }
}