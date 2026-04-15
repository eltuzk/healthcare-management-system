package com.healthcare.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.healthcare.backend.security.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired

    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                        .disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/roles/**", "/permissions/**", "/accounts/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/doctors/**").hasAnyAuthority("ADMIN", "PATIENT", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.POST, "/doctors/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/doctors/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/doctors/**").hasAuthority("ADMIN")
                        .anyRequest()
                        .authenticated())
                //.httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
                // .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
