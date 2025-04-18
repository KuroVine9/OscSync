@file:OptIn(ExperimentalEncodingApi::class)

package dev.kuro9.domain.member.auth.jwt

import dev.kuro9.domain.member.auth.config.JwtTokenConfigProperties
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.multiplatform.common.serialization.minifyJson
import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import kotlinx.serialization.SerializationException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

@Service
class JwtTokenService(
    private val properties: JwtTokenConfigProperties,
) {
    private val noPaddingBase64 = Base64.withPadding(Base64.PaddingOption.ABSENT)
    private val accessTokenExpireDuration = 30.minutes

    fun makeToken(authentication: Authentication): JwtToken {
        val userDetail = authentication.principal as DiscordUserDetail
        val payload = JwtPayloadV1(
            sub = userDetail.id.toString(),
            name = userDetail.userName,
            iat = Clock.System.now(),
            exp = Clock.System.now() + accessTokenExpireDuration,
            scp = authentication.authorities.map { it.authority },
            avatarUrl = userDetail.avatarUrl,
        )

        return makeToken(payload, properties.key)
    }

    @Throws(JwtValidationException::class, SerializationException::class)
    fun validateAndGetPayload(token: JwtToken): JwtPayloadV1 {
        return token.validateAndGetPayload(properties.key)
    }

    fun isValid(token: JwtToken): Boolean = token.isValid<JwtPayloadV1>(properties.key)


    private inline fun <reified T : JwtBasicPayload> makeToken(jwtPayload: T, secretKey: String): JwtToken {
        val encodedHeader = """{"alg":"HS512","typ":"JWT"}"""
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val payload = minifyJson.encodeToString(jwtPayload)
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val signature = getSignature(
            encodedHeader = encodedHeader,
            encodedPayload = payload,
            jwtPayload = jwtPayload,
            secretKey = secretKey,
        )

        return JwtToken("$encodedHeader.$payload.$signature")
    }

    @Throws(JwtValidationException::class, SerializationException::class)
    private inline fun <reified T : JwtBasicPayload> JwtToken.validateAndGetPayload(secretKey: String): T {
        val (encodedHeader, encodedPayload, signature) = this.token.split('.')

        val payload = noPaddingBase64.decode(encodedPayload)
            .toString(Charsets.UTF_8)
            .let<String, T>(minifyJson::decodeFromString)

        val expectSignature = getSignature(
            encodedHeader = encodedHeader,
            encodedPayload = encodedPayload,
            jwtPayload = payload,
            secretKey = secretKey,
        )

        if (expectSignature != signature) throw JwtValidationException(
            "jwt is malformed.", listOf(
                OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "jwt is not valid.",
                    null
                )
            )
        )

        return payload
    }

    private inline fun <reified T : JwtBasicPayload> JwtToken.isValid(secretKey: String): Boolean {
        val (encodedHeader, encodedPayload, signature) = this.token.split('.')

        val payload = noPaddingBase64.decode(encodedPayload)
            .toString(Charsets.UTF_8)
            .let<String, T>(minifyJson::decodeFromString)

        val expectSignature = getSignature(
            encodedHeader = encodedHeader,
            encodedPayload = encodedPayload,
            jwtPayload = payload,
            secretKey = secretKey,
        )

        return expectSignature == signature && !payload.isExpired()
    }

    private inline fun <reified T : JwtBasicPayload> getSecretKeyWithSalt(jwtPayload: T, secretKey: String): ByteArray {
        return (jwtPayload.sub.toLong())
            .shr(jwtPayload.iat.epochSeconds.toInt() % 5)
            .times(jwtPayload.exp.epochSeconds.toInt())
            .let {
                val secretKeyBytes = secretKey.toByteArray(Charsets.UTF_8)
                Random(it).nextBytes(secretKeyBytes.size).zip(secretKeyBytes)
                    .map { (a, b) -> a xor b }
                    .toByteArray()
            }
    }

    private inline fun <reified T : JwtBasicPayload> getSignature(
        encodedHeader: String,
        encodedPayload: String,
        jwtPayload: T,
        secretKey: String
    ): String {
        return Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(getSecretKeyWithSalt(jwtPayload, secretKey), algorithm))
            doFinal("$encodedHeader.$encodedPayload".toByteArray(Charsets.UTF_8))
        }
            .encodeWithNoPadding()
    }

    fun ByteArray.encodeWithNoPadding(): String {
        return noPaddingBase64.encode(this)
    }

    private fun JwtBasicPayload.isExpired(): Boolean {
        return System.now() >= exp
    }
}