package back.client_exp_backend.models.enums;

public enum UsageFrequency {
  FIRST_TIME("впервые"),
  FEW_TIMES_A_YEAR("несколько раз в год"),
  ONCE_A_MONTH("раз в месяц"),
  WEEKLY_OR_MORE("раз в неделю и чаще");

  private final String displayValue;

  UsageFrequency(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}