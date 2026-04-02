package co.edu.corhuila.inventory_service.Config;

import co.edu.corhuila.inventory_service.Service.JwtFilter;
import co.edu.corhuila.inventory_service.Service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/status").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Products: ADMIN can create, update and delete
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Products: ADMIN, PHARMACIST and AUDITOR can view
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("ADMIN", "FARMACEUTICO", "AUDITOR")

                        // Motions/Movements: ADMIN and AUDITOR can view
                        .requestMatchers(HttpMethod.GET, "/api/movements/**")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/motions/**")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/Motion/**")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // Everything else requires authentication
                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

