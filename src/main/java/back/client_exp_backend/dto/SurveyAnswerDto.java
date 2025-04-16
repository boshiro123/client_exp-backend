package back.client_exp_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyAnswerDto {

  @NotNull(message = "ID вопроса обязателен")
  private Long questionId;

  // Может быть строкой (для вопросов с одним вариантом ответа или текстовых
  // вопросов)
  // или массивом строк (для вопросов с множественным выбором)
  private Object answer;

  // Вспомогательный метод для получения ответа как строки
  public String getTextAnswer() {
    if (answer == null) {
      return null;
    }
    if (answer instanceof String) {
      return (String) answer;
    }
    if (answer instanceof List) {
      List<?> answerList = (List<?>) answer;
      if (!answerList.isEmpty()) {
        return answerList.get(0).toString();
      }
    }
    return answer.toString();
  }

  // Вспомогательный метод для получения ответа как числа
  public Integer getNumericAnswer() {
    if (answer == null) {
      return null;
    }
    try {
      if (answer instanceof Number) {
        return ((Number) answer).intValue();
      }
      if (answer instanceof String) {
        return Integer.parseInt((String) answer);
      }
    } catch (NumberFormatException e) {
      // Возвращаем null, если не удалось преобразовать в число
      return null;
    }
    return null;
  }

  // Вспомогательный метод для получения списка ответов
  @SuppressWarnings("unchecked")
  public List<String> getAnswerList() {
    if (answer == null) {
      return null;
    }
    if (answer instanceof List) {
      return (List<String>) answer;
    }
    if (answer instanceof String) {
      return List.of((String) answer);
    }
    return List.of(answer.toString());
  }
}