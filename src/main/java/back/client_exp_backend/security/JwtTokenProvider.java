package back.client_exp_backend.security;

import back.client_exp_backend.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpirationInMs;

  private final TokenBlacklist tokenBlacklist;

  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  public String generateToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("id", user.getId())
        .claim("role", user.getRole().name())
        .claim("username", user.getUsername())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    Claims claims = extractAllClaims(token);

    String email = claims.getSubject();
    String role = claims.get("role", String.class);

    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

    return new UsernamePasswordAuthenticationToken(email, null, authorities);
  }

  public boolean validateToken(String token) {
    try {
      // Проверка на черный список
      if (tokenBlacklist.isBlacklisted(token)) {
        log.warn("Попытка использования токена из черного списка");
        return false;
      }

      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.error("JWT токен истек: {}", e.getMessage());
      return false;
    } catch (UnsupportedJwtException e) {
      log.error("Неподдерживаемый JWT токен: {}", e.getMessage());
      return false;
    } catch (MalformedJwtException e) {
      log.error("Неверно сформированный JWT токен: {}", e.getMessage());
      return false;
    } catch (SignatureException e) {
      log.error("Недействительная подпись JWT: {}", e.getMessage());
      return false;
    } catch (IllegalArgumentException e) {
      log.error("Пустой или null JWT токен: {}", e.getMessage());
      return false;
    } catch (JwtException e) {
      log.error("Невалидный JWT токен: {}", e.getMessage());
      return false;
    }
  }

  public String getEmailFromToken(String token) {
    return extractAllClaims(token).getSubject();
  }

  public Long getUserIdFromToken(String token) {
    return extractAllClaims(token).get("id", Long.class);
  }

  public String getUsernameFromToken(String token) {
    return extractAllClaims(token).get("username", String.class);
  }

  public String getRoleFromToken(String token) {
    return extractAllClaims(token).get("role", String.class);
  }

  public Date getExpirationDateFromToken(String token) {
    return extractAllClaims(token).getExpiration();
  }

  public void blacklistToken(String token) {
    Date expiryDate = getExpirationDateFromToken(token);
    tokenBlacklist.addToBlacklist(token, expiryDate.getTime());
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}