package back.client_exp_backend.models.enums;

public enum Gender {
  MALE("мужской"),
  FEMALE("женский");

  private final String displayValue;

  Gender(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}