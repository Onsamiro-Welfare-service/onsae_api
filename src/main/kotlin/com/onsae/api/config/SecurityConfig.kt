package com.onsae.api.config

import com.onsae.api.auth.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
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
                    .requestMatchers("/api/system/login", "/api/system/register").permitAll()
                    .requestMatchers("/api/admin/login", "/api/admin/register").permitAll()
                    .requestMatchers("GET", "/api/institutions").permitAll()
                    .requestMatchers("/api/institutions/**").hasRole("SYSTEM_ADMIN")
                    .requestMatchers("/api/user/login", "/api/user/signup").permitAll()
                    .requestMatchers("/api/user/register").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("GET", "/api/user").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("GET", "/api/user/*/profile").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("PUT", "/api/user/*/profile").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("/api/user-groups/**").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("/api/questions/**").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("/api/question-assignments/**").hasAnyRole("ADMIN", "STAFF")
                    // Admin endpoints - specific patterns for SYSTEM_ADMIN first
                    .requestMatchers("GET", "/api/admin").hasRole("SYSTEM_ADMIN")
                    .requestMatchers("PUT", "/api/admin/*/status").hasRole("SYSTEM_ADMIN")
                    .requestMatchers("/api/admin/approve/**").hasRole("SYSTEM_ADMIN")
                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "STAFF")
                    .requestMatchers("/api/test/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/system/**").hasRole("SYSTEM_ADMIN")
                    .requestMatchers("/api/user/**").hasRole("USER")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}