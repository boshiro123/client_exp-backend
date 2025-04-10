package back.client_exp_backend.config;

import back.client_exp_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Публичные эндпоинты для авторизации
            .requestMatchers("/api/auth/**").permitAll()

            // Публичные эндпоинты для доступа к опросникам
            .requestMatchers("/api/surveys/public/**").permitAll()

            // API пользователей только для админов
            .requestMatchers("/api/users/**").hasRole("ADMIN")

            // API опросников
            .requestMatchers(HttpMethod.GET, "/api/surveys/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers(HttpMethod.POST, "/api/surveys/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/surveys/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/surveys/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers(HttpMethod.PATCH, "/api/surveys/**").hasAnyRole("ADMIN", "MANAGER")

            // Все остальные запросы требуют аутентификации
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(false);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}