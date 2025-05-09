package com.ra.st.security;

import com.ra.st.security.jwt.CustomAccessDeniedHandler;
import com.ra.st.security.jwt.JwtAuthTokenFilter;
import com.ra.st.security.jwt.JwtEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private JwtAuthTokenFilter jwtAuthTokenFilter;
    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;
    @Autowired
    private JwtEntryPoint jwtEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cf -> cf.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedMethods(List.of("*"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("*"));
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Các endpoint permitAll phải được đặt trước các quy tắc yêu cầu xác thực
                        .requestMatchers("/api/v1/user/cart/checkout/success").permitAll()
                        .requestMatchers("/api/v1/user/cart/checkout/cancel").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/paypal/**").permitAll()
                        // Các endpoint yêu cầu quyền
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/user/**").hasAuthority("USER")
                        // Mọi yêu cầu khác cần xác thực
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}