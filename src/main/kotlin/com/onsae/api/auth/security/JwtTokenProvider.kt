package com.onsae.api.auth.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

private val logger = KotlinLogging.logger {}

@Component
class JwtTokenProvider {

    @Value("\${JWT_SECRET:your-secret-key-here-minimum-256-bits-long}")
    private lateinit var jwtSecret: String

    @Value("\${JWT_ACCESS_TOKEN_EXPIRATION:86400000}")
    private var accessTokenExpiration: Long = 86400000 // 24시간

    @Value("\${JWT_REFRESH_TOKEN_EXPIRATION:604800000}")
    private var refreshTokenExpiration: Long = 604800000 // 7일

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateAccessToken(
        userId: Long,
        userType: String,
        institutionId: Long?,
        authorities: List<String>
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("userType", userType)
            .claim("institutionId", institutionId)
            .claim("authorities", authorities)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.subject.toLong()
    }

    fun getUserTypeFromToken(token: String): String? {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims["userType"] as? String
    }

    fun getInstitutionIdFromToken(token: String): Long? {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return (claims["institutionId"] as? Number)?.toLong()
    }

    @Suppress("UNCHECKED_CAST")
    fun getAuthoritiesFromToken(token: String): List<String> {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims["authorities"] as? List<String> ?: emptyList()
    }

    fun getAuthentication(token: String): Authentication {
        val userId = getUserIdFromToken(token)
        val authorities = getAuthoritiesFromToken(token)
            .map { SimpleGrantedAuthority(it) }

        val principal = CustomUserPrincipal(
            userId = userId,
            userType = getUserTypeFromToken(token) ?: "",
            institutionId = getInstitutionIdFromToken(token),
            authorities = authorities
        )

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            return true
        } catch (ex: SecurityException) {
            logger.error("Invalid JWT signature: ${ex.message}")
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token: ${ex.message}")
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token: ${ex.message}")
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token: ${ex.message}")
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty: ${ex.message}")
        }
        return false
    }

    fun getExpirationDateFromToken(token: String): Date {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.expiration
    }
}