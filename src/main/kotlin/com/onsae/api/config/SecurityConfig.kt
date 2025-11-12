package com.onsae.api.config

import com.onsae.api.auth.security.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Value("\${app.swagger.security.enabled:false}")
    private var swaggerSecurityEnabled: Boolean = false

    @Value("\${app.swagger.security.username:admin}")
    private lateinit var swaggerUsername: String

    @Value("\${app.swagger.security.password:admin}")
    private lateinit var swaggerPassword: String

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun swaggerUserDetailsService(): UserDetailsService {
        val user = User.builder()
            .username(swaggerUsername)
            .password(passwordEncoder().encode(swaggerPassword))
            .roles("SWAGGER_USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        // Swagger 보안 활성화 시 HTTP Basic Auth 사용
        if (swaggerSecurityEnabled) {
            http.httpBasic { }
        } else {
            http.httpBasic { it.disable() }
        }

        return http
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
                    // ID는 숫자만 허용
                    .requestMatchers("DELETE", "/api/user/{id:[0-9]+}").hasAnyRole("ADMIN", "STAFF")
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
                    // 파일 경로 허용
                    .requestMatchers("/api/files/**").permitAll()
                    // Swagger 경로는 환경에 따라 보호 여부 결정
                    .apply {
                        if (swaggerSecurityEnabled) {
                            this.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                                .hasRole("SWAGGER_USER")
                        } else {
                            this.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                                .permitAll()
                        }
                    }
                    .requestMatchers("/api/user/**").hasRole("USER")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}