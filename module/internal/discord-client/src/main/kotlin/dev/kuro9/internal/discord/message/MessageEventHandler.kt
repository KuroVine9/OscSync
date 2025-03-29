package dev.kuro9.internal.discord.message

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.message.model.MentionedMessageHandler
import dev.kuro9.internal.discord.model.DiscordEventHandler
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(MentionedMessageHandler::class)
internal class MessageEventHandler(
    private val handler: MentionedMessageHandler,
) : DiscordEventHandler<MessageReceivedEvent> {
    override val kClass = MessageReceivedEvent::class

    override suspend fun handle(event: MessageReceivedEvent) {
        Message.suppressContentIntentWarning()
        if (event.author.isBot) return
        val mentionString = event.jda.selfUser.asMention
        val message = event.message.contentRaw
        infoLog(message)
        when {
            event.message.isFromGuild && message.startsWith(mentionString) -> handler.handleMention(
                event, message.removePrefix(mentionString)
            )

            !event.message.isFromGuild -> handler.handleMention(
                event, message.removePrefix(mentionString)
            )
        }
    }
}