package com.innowise.userservice.config;

import com.innowise.userservice.converter.RoleConverter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new RoleConverter());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers(HttpMethod.POST, "/api/users")
                                    .hasAnyRole("user", "admin")
                                .requestMatchers(HttpMethod.GET, "/api/users/me")
                                    .hasAnyRole("user", "admin")
                                .requestMatchers(HttpMethod.GET, "/api/users")
                                    .hasRole("admin")
                                .requestMatchers(HttpMethod.PATCH, "/api/users")
                                    .hasAnyRole("user", "admin")
                                .requestMatchers(HttpMethod.PATCH, "/api/users/{id}/activate")
                                    .hasRole("admin")
                                .requestMatchers(HttpMethod.PATCH, "/api/users/{id}/deactivate")
                                    .hasRole("admin")

                                .requestMatchers(HttpMethod.GET, "/api/cards/user")
                                    .hasAnyRole("user", "admin")
                                .requestMatchers(HttpMethod.POST, "/api/cards")
                                    .hasRole("admin")
                                .requestMatchers(HttpMethod.GET, "/api/cards/{id}")
                                    .hasRole( "admin")
                                .requestMatchers(HttpMethod.GET, "/api/cards")
                                    .hasRole("admin")
                                .requestMatchers(HttpMethod.PATCH, "/api/cards/{id}")
                                    .hasRole("admin")
                                .requestMatchers(HttpMethod.PATCH, "/api/cards/{id}/activate")
                                    .hasRole("admin")
                                .requestMatchers(HttpMethod.PATCH, "/api/cards/{id}/deactivate")
                                    .hasRole("admin")

                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2ResourceServer(oauth2 -> {
                            oauth2.jwt(jwt ->
                                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter));
                        }
                );

        return http.build();
    }

}
