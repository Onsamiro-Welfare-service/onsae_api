package com.onsae.api.config

import com.onsae.api.auth.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/auth/refresh", "/api/auth/logout").permitAll()
                    .requestMatchers("/api/system/login").permitAll()
                    .requestMatchers("/api/admin/login", "/api/admin/register").permitAll()
                    .requestMatchers("/api/user/login").permitAll()
                    .requestMatchers("/api/user/register").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("/api/admin/pending", "/api/admin/approve/**").hasRole("SYSTEM_ADMIN")
                    .requestMatchers("/api/test/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/system/**").hasRole("SYSTEM_ADMIN")
                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("/api/user/**").hasRole("USER")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}