package back.client_exp_backend.models.enums;

public enum InteractionType {
  SURVEY("опрос"),
  FEEDBACK("отзыв"),
  INQUIRY("обращение"),
  PURCHASE("покупка");

  private final String displayValue;

  InteractionType(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}