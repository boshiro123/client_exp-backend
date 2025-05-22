package back.client_exp_backend.controller;

import back.client_exp_backend.dto.AnswerOptionDto;
import back.client_exp_backend.dto.ApiResponse;
import back.client_exp_backend.service.AnswerOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerOptionController {

  private final AnswerOptionService answerOptionService;

  @GetMapping("/questions/{questionId}/options")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<List<AnswerOptionDto>> getAnswerOptionsByQuestionId(@PathVariable Long questionId) {
    return ResponseEntity.ok(answerOptionService.getAnswerOptionsByQuestionId(questionId));
  }

  @GetMapping("/options/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<AnswerOptionDto> getAnswerOptionById(@PathVariable Long id) {
    return ResponseEntity.ok(answerOptionService.getAnswerOptionById(id));
  }

  @PostMapping("/questions/{questionId}/options")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<AnswerOptionDto> createAnswerOption(
      @PathVariable Long questionId,
      @Valid @RequestBody AnswerOptionDto answerOptionDto) {
    AnswerOptionDto createdOption = answerOptionService.createAnswerOption(questionId, answerOptionDto);
    return new ResponseEntity<>(createdOption, HttpStatus.CREATED);
  }

  @PutMapping("/options/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<AnswerOptionDto> updateAnswerOption(
      @PathVariable Long id,
      @Valid @RequestBody AnswerOptionDto answerOptionDto) {
    AnswerOptionDto updatedOption = answerOptionService.updateAnswerOption(id, answerOptionDto);
    return ResponseEntity.ok(updatedOption);
  }

  @DeleteMapping("/options/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<ApiResponse> deleteAnswerOption(@PathVariable Long id) {
    answerOptionService.deleteAnswerOption(id);
    return ResponseEntity.ok(new ApiResponse(true, "Вариант ответа успешно удален"));
  }
}