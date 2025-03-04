package dev.kuro9.domain.member.auth.jwt

import dev.kuro9.multiplatform.common.serialization.serializer.instant.UnixTimestamp

interface JwtBasicPayload {
    /** 유저 구별자 */
    val sub: String

    /** 발급일 (issued at) */
    val iat: UnixTimestamp

    /** 만료일 (expired at) */
    val exp: UnixTimestamp
}