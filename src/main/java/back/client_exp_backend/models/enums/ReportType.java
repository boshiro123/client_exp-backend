package back.client_exp_backend.models.enums;

public enum ReportType {
  SATISFACTION("удовлетворенность"),
  SEGMENTATION("сегментация"),
  GENERAL_ANALYTICS("общая аналитика");

  private final String displayValue;

  ReportType(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}