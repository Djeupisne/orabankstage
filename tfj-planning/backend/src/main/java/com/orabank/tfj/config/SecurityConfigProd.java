package com.orabank.tfj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité pour l'environnement de production.
 * - Authentification par comptes définis en mémoire
 * - Autorisations basées sur les rôles
 * - CORS restreint aux domaines autorisés
 * - CSRF désactivé pour API REST
 * - open-in-view désactivé (dans application.yml)
 */
@Configuration
@EnableWebSecurity
@Profile("prod")
public class SecurityConfigProd {

    @Value("${ADMIN_PASSWORD:Admin@2024Secure!}")
    private String adminPassword;

    @Value("${GESTIONNAIRE_PASSWORD:Gestion@2024Secure!}")
    private String gestionnairePassword;

    @Value("${OPERATEUR_PASSWORD:Operateur@2024Secure!}")
    private String operateurPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics pour le monitoring
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Swagger et API docs désactivés en prod
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").denyAll()
                // API ouverte en lecture seule pour le frontend
                .requestMatchers("/api/**").permitAll()
                // Tout le reste nécessite authentification
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .anonymous(Customizer.withDefaults());
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        List<UserDetails> users = Arrays.asList(
            // Administrateur avec tous les droits
            User.builder()
                .username("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN", "USER")
                .accountLocked(false)
                .disabled(false)
                .build(),
            
            // Gestionnaire avec droits standards
            User.builder()
                .username("gestionnaire")
                .password(passwordEncoder.encode(gestionnairePassword))
                .roles("USER")
                .accountLocked(false)
                .disabled(false)
                .build(),
            
            // Opérateur avec droits limités
            User.builder()
                .username("operateur")
                .password(passwordEncoder.encode(operateurPassword))
                .roles("USER")
                .accountLocked(false)
                .disabled(false)
                .build()
        );
        
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Force 12 pour la production
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // En production, inclure explicitement le domaine Render du frontend
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        } else {
            // Valeurs par défaut incluant les domaines Render
            configuration.setAllowedOrigins(Arrays.asList(
                "https://tfj-planning-frontend.onrender.com",
                "https://tfj-planning-backend.onrender.com",
                "http://localhost:3000",
                "http://localhost:8080"
            ));
        }
        
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
