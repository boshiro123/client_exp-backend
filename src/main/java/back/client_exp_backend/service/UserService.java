package back.client_exp_backend.service;

import back.client_exp_backend.dto.UserDto;
import back.client_exp_backend.models.User;
import back.client_exp_backend.models.enums.UserRole;

import java.util.List;

public interface UserService {
  User findByEmail(String email);

  User findById(Long id);

  boolean existsByEmail(String email);

  User save(User user);

  void delete(User user);

  void delete(Long userId);

  User updateUserRole(Long userId, UserRole role);

  List<User> findByRole(UserRole role);

  UserDto convertToDto(User user);
}