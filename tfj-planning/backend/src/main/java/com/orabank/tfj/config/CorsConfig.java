package com.orabank.tfj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Autoriser le frontend sur Render
        config.addAllowedOrigin("https://tfj-planning-frontend.onrender.com");
        
        // Autoriser toutes les méthodes HTTP
        config.addAllowedMethod("*");
        
        // Autoriser tous les headers
        config.addAllowedHeader("*");
        
        // Autoriser l'envoi de credentials (cookies, etc.)
        config.setAllowCredentials(true);
        
        // Exposition des headers
        config.addExposedHeader("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
