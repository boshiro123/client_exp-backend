package back.client_exp_backend.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
  private HttpStatus status;
  private int statusCode;
  private String message;
  private List<String> errors;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime timestamp;

  public ApiError(HttpStatus status, String message, List<String> errors) {
    this.timestamp = LocalDateTime.now();
    this.status = status;
    this.statusCode = status.value();
    this.message = message;
    this.errors = errors;
  }

  public ApiError(HttpStatus status, String message, String error) {
    this.timestamp = LocalDateTime.now();
    this.status = status;
    this.statusCode = status.value();
    this.message = message;
    this.errors = List.of(error);
  }
}