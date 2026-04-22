package com.orabank.tfj.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exc -> exc
                .authenticationEntryPoint((request, response, authException) -> {
                    String origin = request.getHeader("Origin");
                    if (origin != null) {
                        response.setHeader("Access-Control-Allow-Origin", origin);
                        response.setHeader("Access-Control-Allow-Credentials", "true");
                    }
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Non autorise\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String origin = request.getHeader("Origin");
                    if (origin != null) {
                        response.setHeader("Access-Control-Allow-Origin", origin);
                        response.setHeader("Access-Control-Allow-Credentials", "true");
                    }
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Acces refuse\"}");
                })
            )
            .httpBasic(basic -> basic.disable())
            .anonymous(Customizer.withDefaults());

        // IMPORTANT: Ajouter le filtre JWT APRES la configuration d'autorisation
        http.addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // IMPORTANT: Ne pas utiliser "*" avec allowCredentials(true)
        // Utiliser uniquement des origins spécifiques
        config.setAllowedOriginPatterns(List.of(
            "https://tfj-planning-frontend.onrender.com",
            "http://localhost:4200",
            "http://localhost:3000"
        ));
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Disposition", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appliquer la configuration CORS à TOUS les endpoints
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
