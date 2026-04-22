package com.orabank.tfj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TfjPlanningApplication {

    public static void main(String[] args) {
        SpringApplication.run(TfjPlanningApplication.class, args);
    }

    /**
     * Configuration CORS globale pour Spring MVC
     * Permet toutes les origines pour Render
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .exposedHeaders("Authorization", "Content-Disposition", "Access-Control-Allow-Origin")
                    .allowCredentials(false)
                    .maxAge(3600);
            }
        };
    }
}
