package com.onsae.api.user.service

import mu.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

@Service
class TemporaryLoginCodeService(
    private val cacheManager: CacheManager
) {

    companion object {
        private const val CACHE_NAME = "loginCodes"
        private const val USER_CODE_PREFIX = "user:"
        private const val CODE_PREFIX = "code:"
    }

    fun generateTemporaryCode(userId: Long): String {
        logger.info("Generating temporary code for user: $userId")

        // 기존 사용자 코드가 있으면 제거
        invalidateUserCode(userId)

        var code: String
        var attempts = 0
        val maxAttempts = 100

        // 중복되지 않는 코드 생성 (최대 100번 시도)
        do {
            code = generateRandomCode()
            attempts++
            logger.debug("Generated code candidate: $code (attempt $attempts)")
        } while (isCodeExists(code) && attempts < maxAttempts)

        if (attempts >= maxAttempts) {
            throw RuntimeException("임시 로그인 코드 생성에 실패했습니다. 다시 시도해주세요.")
        }

        // 캐시에 저장: code -> userId, user -> code 양방향 매핑
        val cache = cacheManager.getCache(CACHE_NAME)!!
        val codeKey = "$CODE_PREFIX$code"
        val userKey = "$USER_CODE_PREFIX$userId"

        cache.put(codeKey, userId)
        cache.put(userKey, code)

        // 저장 확인
        val verifyUserId = cache.get(codeKey)?.get() as? Long
        val verifyCode = cache.get(userKey)?.get() as? String

        logger.info("Generated temporary login code: $code for user: $userId")
        logger.debug("Cache verification - codeKey: $codeKey -> $verifyUserId, userKey: $userKey -> $verifyCode")

        return code
    }

    fun validateAndConsumeCode(code: String): Long? {
        logger.info("Validating temporary login code: $code")

        val cache = cacheManager.getCache(CACHE_NAME)!!
        val codeKey = "$CODE_PREFIX$code"

        // 캐시 상태 디버깅
        logger.debug("Looking for key: $codeKey in cache: $CACHE_NAME")

        val userId = getUserIdByCode(code)
        logger.info("Found userId: $userId for code: $code")

        if (userId != null) {
            // 코드 사용 후 즉시 제거 (일회용)
            invalidateCode(code)
            invalidateUserCode(userId)
            logger.info("Consumed temporary login code: $code for user: $userId")
        } else {
            logger.warn("Invalid or expired temporary login code: $code")
            // 캐시에 뭐가 있는지 확인해보기 위한 디버깅
            logger.debug("Cache debug - attempting direct cache lookup")
            val directLookup = cache.get(codeKey)
            logger.debug("Direct cache lookup result: $directLookup")
        }
        return userId
    }

    fun invalidateUserCode(userId: Long) {
        val existingCode = getUserCode(userId)
        if (existingCode != null) {
            val cache = cacheManager.getCache(CACHE_NAME)!!
            cache.evict("$CODE_PREFIX$existingCode")
            cache.evict("$USER_CODE_PREFIX$userId")
            logger.debug("Invalidated existing code for user: $userId")
        }
    }

    private fun getUserIdByCode(code: String): Long? {
        return cacheManager.getCache(CACHE_NAME)?.get("$CODE_PREFIX$code")?.get() as? Long
    }

    private fun getUserCode(userId: Long): String? {
        return cacheManager.getCache(CACHE_NAME)?.get("$USER_CODE_PREFIX$userId")?.get() as? String
    }

    private fun isCodeExists(code: String): Boolean {
        return getUserIdByCode(code) != null
    }

    @CacheEvict(value = [CACHE_NAME], key = "'$CODE_PREFIX' + #code")
    private fun invalidateCode(code: String) {
        // 캐시에서 제거
    }

    private fun generateRandomCode(): String {
        return String.format("%04d", Random.nextInt(10000))
    }
}