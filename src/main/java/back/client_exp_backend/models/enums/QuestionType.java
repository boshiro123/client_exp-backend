package back.client_exp_backend.models.enums;

public enum QuestionType {
  SINGLE_CHOICE("single_choice"),
  MULTIPLE_CHOICE("multiple_choice"),
  TEXT("text"),
  RATING("rating");

  private final String displayValue;

  QuestionType(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}