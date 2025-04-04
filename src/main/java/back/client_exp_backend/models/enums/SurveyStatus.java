package back.client_exp_backend.models.enums;

public enum SurveyStatus {
  ACTIVE("активный"),
  COMPLETED("завершенный"),
  DRAFT("черновик");

  private final String displayValue;

  SurveyStatus(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}