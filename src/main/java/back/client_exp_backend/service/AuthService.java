package back.client_exp_backend.service;

import back.client_exp_backend.dto.AuthResponse;
import back.client_exp_backend.dto.LoginRequest;
import back.client_exp_backend.dto.LogoutResponse;
import back.client_exp_backend.dto.RegisterRequest;
import back.client_exp_backend.dto.TokenValidationResponse;

public interface AuthService {
  AuthResponse register(RegisterRequest registerRequest);

  AuthResponse login(LoginRequest loginRequest);

  TokenValidationResponse validateToken(String token);

  LogoutResponse logout(String token);
}