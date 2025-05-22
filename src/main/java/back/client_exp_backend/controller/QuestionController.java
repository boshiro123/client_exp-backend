package back.client_exp_backend.controller;

import back.client_exp_backend.dto.ApiResponse;
import back.client_exp_backend.dto.QuestionDto;
import back.client_exp_backend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

  private final QuestionService questionService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<List<QuestionDto>> getAllQuestions() {
    return ResponseEntity.ok(questionService.getAllQuestions());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Long id) {
    return ResponseEntity.ok(questionService.getQuestionById(id));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<QuestionDto> createQuestion(@Valid @RequestBody QuestionDto questionDto) {
    QuestionDto createdQuestion = questionService.createQuestion(questionDto);
    return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<QuestionDto> updateQuestion(
      @PathVariable Long id,
      @Valid @RequestBody QuestionDto questionDto) {
    QuestionDto updatedQuestion = questionService.updateQuestion(id, questionDto);
    return ResponseEntity.ok(updatedQuestion);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<ApiResponse> deleteQuestion(@PathVariable Long id) {
    questionService.deleteQuestion(id);
    return ResponseEntity.ok(new ApiResponse(true, "Вопрос успешно удален"));
  }
}