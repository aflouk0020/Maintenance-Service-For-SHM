package com.sigma.smarthome.maintenance_service.config;

import com.sigma.smarthome.maintenance_service.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/maintenance-requests")
                        .hasAnyRole("PROPERTY_MANAGER", "MAINTENANCE_STAFF")

                        .requestMatchers(HttpMethod.GET, "/api/v1/maintenance-requests")
                        .hasRole("PROPERTY_MANAGER")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/maintenance-requests/*/assign")
                        .hasRole("PROPERTY_MANAGER")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/maintenance-requests/*/status")
                        .hasRole("MAINTENANCE_STAFF")

                        .requestMatchers(HttpMethod.GET, "/api/v1/maintenance-requests/**")
                        .hasAnyRole("PROPERTY_MANAGER", "MAINTENANCE_STAFF")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}