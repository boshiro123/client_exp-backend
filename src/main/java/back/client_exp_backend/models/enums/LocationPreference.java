package back.client_exp_backend.models.enums;

public enum LocationPreference {
  VERY_IMPORTANT("очень важно"),
  NOT_IMPORTANT("не имеет значения"),
  ONLINE_PREFERRED("предпочитаю онлайн");

  private final String displayValue;

  LocationPreference(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}