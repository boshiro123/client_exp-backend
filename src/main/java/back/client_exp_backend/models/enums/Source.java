package back.client_exp_backend.models.enums;

public enum Source {
  SOCIAL_NETWORKS("социальные сети"),
  RECOMMENDATIONS("рекомендации друзей"),
  INTERNET_SEARCH("поиск в интернете"),
  OTHER("другое");

  private final String displayValue;

  Source(String displayValue) {
    this.displayValue = displayValue;
  }

  public String getDisplayValue() {
    return displayValue;
  }
}