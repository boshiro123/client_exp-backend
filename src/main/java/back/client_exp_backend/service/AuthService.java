package back.client_exp_backend.service;

import back.client_exp_backend.dto.AuthResponse;
import back.client_exp_backend.dto.LoginRequest;
import back.client_exp_backend.dto.RegisterRequest;

public interface AuthService {
  AuthResponse register(RegisterRequest registerRequest);

  AuthResponse login(LoginRequest loginRequest);
}