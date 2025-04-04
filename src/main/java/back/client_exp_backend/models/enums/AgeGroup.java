package back.client_exp_backend.models.enums;

public enum AgeGroup {
  UNDER_18("до 18 лет"),
  FROM_18_TO_25("18-25 лет"),
  FROM_26_TO_35("26-35 лет"),
  FROM_36_TO_45("36-45 лет"),
  FROM_46_TO_60("46-60 лет"),
  OVER_60("60+ лет");

  private final String displayValue;

  AgeGroup(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}