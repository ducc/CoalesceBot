package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.punishments.ForcedPunishment
import com.coalesce.punishments.Punishment
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.*

@Command(name = "Warn", permission = "commands.warn", description = "Allows for warning a user")
class Warn : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (!message.guild.getMember(message.author).roles.contains(Bot.instance.jda.getRoleById("268239031467376640"))) {
            channel.sendMessage("You lack permission to use this command.").queue()
            return
        }

        if(message.mentionedUsers.size != 1){
            channel.sendMessage("You need to mention a user to use this command.").queue()
            return
        }

        if(args.isEmpty()){
            channel.sendMessage("Invalid usage, proper usage is: !warn <mention> [description]").queue()
            return
        }
        val user = message.mentionedUsers[0]

        var description : String? = null
        if (args.size > 1) {
            val desc = StringBuilder()
            Arrays.asList(args).subList(1, args.size).forEach { desc.append(it + " ") }
            description = desc.toString()
        }

        val history = Bot.instance.manager.findPunishments(message.author)
        val punishment = ForcedPunishment(true, null, message.author, message.author.id, description)

        val newHistory = punishment.doActUpon(history, user, channel)
        Bot.instance.manager.saveChanges(user, newHistory)
    }
}