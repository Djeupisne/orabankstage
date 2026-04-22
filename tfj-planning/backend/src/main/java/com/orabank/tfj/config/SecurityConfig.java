package com.orabank.tfj.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/**").permitAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Utiliser "*" pour permettre toutes les origines (plus simple pour Render)
        // Note: allowedOriginPatterns avec "*" ne fonctionne pas avec allowCredentials(true)
        // On utilise donc une liste explicite d'origines autorisées
        config.setAllowedOrigins(List.of(
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
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
    /**
     * Configuration CORS supplémentaire pour Spring MVC
     * Cette configuration s'applique avant le filtre de sécurité Spring Security
     */
    @Bean
    public org.springframework.web.filter.CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Utiliser la même configuration que ci-dessus
        config.setAllowedOrigins(List.of(
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
        
        source.registerCorsConfiguration("/**", config);
        return new org.springframework.web.filter.CorsFilter(source);
    }

    /**
     * Configuration CORS additionnelle via WebMvcConfigurer
     * Pour s'assurer que CORS fonctionne correctement avec Spring MVC
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(
                        "https://tfj-planning-frontend.onrender.com",
                        "http://localhost:4200",
                        "http://localhost:3000"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .exposedHeaders("Authorization", "Content-Disposition", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
