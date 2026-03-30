package co.edu.corhuila.auth_service.Config;


import co.edu.corhuila.auth_service.Service.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // SOLO LOGIN ES PUBLICO
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/status").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // CAMBIO DE CONTRASEÑA: primero la ruta específica
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/password")
                        .hasAnyRole("ADMIN", "AUDITOR", "FARMACEUTICO")

                        // ACTUALIZAR USUARIO
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/update")
                        .hasAnyRole("ADMIN", "AUDITOR", "FARMACEUTICO")

                        // CONSULTAR USUARIOS
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // BINNACLE
                        .requestMatchers("/api/binnacle/**")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // RESTO DE /api/users/**
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")
                    


                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
