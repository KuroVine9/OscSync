package dev.kuro9.domain.smartapp.user.service

import dev.kuro9.domain.smartapp.user.exception.SmartAppUserException.CredentialNotFoundException
import dev.kuro9.domain.smartapp.user.repository.SmartAppUserCredentialEntity
import dev.kuro9.domain.smartapp.user.repository.SmartAppUserCredentials
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppToken
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class SmartAppCredentialService(private val database: Database) {

    /**
     * @return user smartapp token or null
     */
    fun getUserCredentialOrNull(userId: Long): SmartAppToken? {
        return SmartAppUserCredentialEntity.findById(userId)?.smartAppToken?.let(SmartAppToken::of)
    }

    /**
     * @return user smartapp token
     */
    @Throws(CredentialNotFoundException::class)
    fun getUserCredential(userId: Long): SmartAppToken =
        getUserCredentialOrNull(userId) ?: throw CredentialNotFoundException("User token not found")

    @Transactional
    fun saveUserCredential(userId: Long, smartAppToken: String) {
        transaction(database) {
            SmartAppUserCredentials.upsert {
                it[SmartAppUserCredentials.userId] = userId
                it[SmartAppUserCredentials.smartAppToken] = smartAppToken
            }
        }
    }

    @Transactional
    fun deleteUserCredential(userId: Long) {
        transaction(database) {
            SmartAppUserCredentials.deleteWhere { SmartAppUserCredentials.userId eq userId }
        }
    }
}