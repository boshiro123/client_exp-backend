package back.client_exp_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;
  private final TokenBlacklist tokenBlacklist;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request);

      if (StringUtils.hasText(jwt)) {
        // Проверяем, не в черном ли списке токен
        if (tokenBlacklist.isBlacklisted(jwt)) {
          log.warn("Попытка использования токена из черного списка. URI: {}", request.getRequestURI());
          // Продолжаем фильтрацию без установки аутентификации
        } else if (tokenProvider.validateToken(jwt)) {
          Authentication authentication = tokenProvider.getAuthentication(jwt);
          SecurityContextHolder.getContext().setAuthentication(authentication);
          log.debug("Пользователь аутентифицирован с токеном JWT. URI: {}", request.getRequestURI());
          log.debug("Информация об аутентификации: username={}, authorities={}",
              authentication.getName(),
              authentication.getAuthorities());
        } else {
          log.warn("Недействительный JWT токен. URI: {}", request.getRequestURI());
        }
      }
    } catch (Exception ex) {
      log.error("Не удалось установить аутентификацию пользователя: {}", ex.getMessage());
      SecurityContextHolder.clearContext(); // Очищаем контекст безопасности при ошибке
    }

    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}