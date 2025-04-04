package back.client_exp_backend.models.enums;

public enum ReportFormat {
  PDF("PDF"),
  EXCEL("Excel");

  private final String displayValue;

  ReportFormat(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}