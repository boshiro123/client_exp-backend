package back.client_exp_backend.service;

import back.client_exp_backend.dto.UserDto;
import back.client_exp_backend.models.User;

public interface UserService {
  User findByEmail(String email);

  boolean existsByEmail(String email);

  User save(User user);

  UserDto convertToDto(User user);
}