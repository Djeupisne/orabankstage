package com.orabank.tfj.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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

/**
 * Configuration de sécurité pour l'environnement de production.
 * - Authentification par JWT via la base de données
 * - Autorisations basées sur les rôles
 * - CORS ouvert pour le frontend
 * - CSRF désactivé pour API REST
 */
@Configuration
@EnableWebSecurity
@Profile("prod")
@RequiredArgsConstructor
public class SecurityConfigProd {

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
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics pour le monitoring
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Swagger et API docs désactivés en prod
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").denyAll()
                // Endpoints d'authentification publics (login)
                .requestMatchers("/api/auth/login").permitAll()
                // API ouverte en lecture seule pour le frontend - GET uniquement
                .requestMatchers("GET", "/api/**").permitAll()
                // Autres méthodes HTTP sur API nécessitent authentification
                .requestMatchers("/api/**").authenticated()
                // Tout le reste nécessite authentification
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .anonymous(Customizer.withDefaults());
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Autoriser tous les domaines en production (à restreindre si nécessaire)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
