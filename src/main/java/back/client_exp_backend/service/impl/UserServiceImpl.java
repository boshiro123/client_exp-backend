package back.client_exp_backend.service.impl;

import back.client_exp_backend.dto.UserDto;
import back.client_exp_backend.exception.ResourceNotFoundException;
import back.client_exp_backend.models.User;
import back.client_exp_backend.repository.UserRepository;
import back.client_exp_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "email", email));
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public User save(User user) {
    return userRepository.save(user);
  }

  @Override
  public UserDto convertToDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .build();
  }
}