package dev.kuro9.application.discord.event

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component

@Component
class DiscordOnReadyEvent(private val database: Database) : DiscordEventHandler<ReadyEvent> {
    override val kClass = ReadyEvent::class

    override suspend fun handle(event: ReadyEvent) {
        transaction(database) {
            infoLog("discord client ready")
            Table.Dual.select(stringLiteral("hello, world!"))
                .first()
                .get(stringLiteral("hello, world!"))
                .also { infoLog("db : $it") }
        }
    }
}