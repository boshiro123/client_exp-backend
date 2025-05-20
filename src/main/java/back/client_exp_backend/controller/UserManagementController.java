package back.client_exp_backend.controller;

import back.client_exp_backend.dto.UserApprovalRequest;
import back.client_exp_backend.dto.UserApprovalResponse;
import back.client_exp_backend.dto.UserDto;
import back.client_exp_backend.models.User;
import back.client_exp_backend.models.enums.UserRole;
import back.client_exp_backend.service.AuthService;
import back.client_exp_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

  private final UserService userService;
  private final AuthService authService;

  @GetMapping("/pending")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<UserDto>> getPendingUsers() {
    log.info("Получение списка пользователей со статусом PENDING");
    List<User> pendingUsers = userService.findByRole(UserRole.PENDING);
    List<UserDto> userDtos = pendingUsers.stream()
        .map(userService::convertToDto)
        .collect(Collectors.toList());
    return ResponseEntity.ok(userDtos);
  }

  @PostMapping("/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserApprovalResponse> approveUser(@RequestBody UserApprovalRequest request) {
    log.info("Одобрение пользователя с ID: {}", request.getUserId());
    return ResponseEntity.ok(authService.approveUser(request));
  }

  @DeleteMapping("/{userId}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserApprovalResponse> rejectUser(@PathVariable Long userId) {
    log.info("Отклонение пользователя с ID: {}", userId);
    return ResponseEntity.ok(authService.rejectUser(userId));
  }

  @GetMapping("/managers")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<UserDto>> getManagers() {
    log.info("Получение списка пользователей со статусом MANAGER");
    List<User> managers = userService.findByRole(UserRole.MANAGER);
    List<UserDto> userDtos = managers.stream()
        .map(userService::convertToDto)
        .collect(Collectors.toList());
    return ResponseEntity.ok(userDtos);
  }

  @DeleteMapping("/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
    log.info("Удаление пользователя с ID: {}", userId);
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }
}