package back.client_exp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponseDto<T> {
  private List<T> content;
  private long totalElements;
  private int totalPages;
  private int size;
  private int number;
}