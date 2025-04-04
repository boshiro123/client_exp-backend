package back.client_exp_backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TokenBlacklist {

  // Храним токены и время их истечения
  private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

  /**
   * Добавляет токен в черный список
   * 
   * @param token            Токен для добавления
   * @param expiryTimeMillis Время истечения токена в миллисекундах
   */
  public void addToBlacklist(String token, long expiryTimeMillis) {
    blacklistedTokens.put(token, expiryTimeMillis);
    log.debug("Токен добавлен в черный список. Истекает: {}", Instant.ofEpochMilli(expiryTimeMillis));
  }

  /**
   * Проверяет, находится ли токен в черном списке
   * 
   * @param token Токен для проверки
   * @return true если токен в черном списке, иначе false
   */
  public boolean isBlacklisted(String token) {
    return blacklistedTokens.containsKey(token);
  }

  /**
   * Удаляет просроченные токены из черного списка
   * Выполняется каждый час
   */
  @Scheduled(fixedRate = 3600000) // Каждый час
  public void cleanupBlacklist() {
    long now = System.currentTimeMillis();
    int initialSize = blacklistedTokens.size();

    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);

    int removed = initialSize - blacklistedTokens.size();
    if (removed > 0) {
      log.info("Удалено {} просроченных токенов из черного списка", removed);
    }
  }
}