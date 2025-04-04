package back.client_exp_backend.models.enums;

public enum UsagePurpose {
  PERSONAL("для личных нужд"),
  BUSINESS("для работы/бизнеса");

  private final String displayValue;

  UsagePurpose(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}