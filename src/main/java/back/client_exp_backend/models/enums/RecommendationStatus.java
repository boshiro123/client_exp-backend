package back.client_exp_backend.models.enums;

public enum RecommendationStatus {
  NEW("новая"),
  VIEWED("просмотрена"),
  APPLIED("применена");

  private final String displayValue;

  RecommendationStatus(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}