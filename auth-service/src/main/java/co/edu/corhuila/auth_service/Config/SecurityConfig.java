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

                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("AUDITOR")

                        

                        // Password endpoints
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/password")
                        .hasAnyRole("ADMIN", "AUDITOR", "FARMACEUTICO")
                        
                        // actualizar usuario
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/update")
                        .hasAnyRole("ADMIN", "AUDITOR", "FARMACEUTICO")

                        
                        // Binnacle endpoints
                        .requestMatchers("/api/binnacle/**")
                        .hasAnyRole("ADMIN", "AUDITOR")
                    


                        
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
