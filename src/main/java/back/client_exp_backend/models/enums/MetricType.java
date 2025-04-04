package back.client_exp_backend.models.enums;

public enum MetricType {
  CSAT("CSAT"),
  NPS("NPS"),
  CES("CES"),
  NONE("none");

  private final String displayValue;

  MetricType(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}