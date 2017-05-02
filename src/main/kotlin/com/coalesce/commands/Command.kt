package com.coalesce.commands

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

enum class CommandType {
    FUN, INFORMATION, ADMINISTRATION, MUSIC, DEBUG
}

annotation class Command(
        val name: String,
        val permission: String,
        val usage: String = "",
        val description: String = "",
        val aliases: Array<String> = arrayOf(),
        val type: CommandType,
        val cooldown: Int = 0
)

class CommandError(message: String) : Exception(message) {
    constructor(message: String, vararg obj: Any) : this(String.format(message, obj))
}

abstract class CommandExecutor {
    internal lateinit var jda: JDA
    internal lateinit var commandMap: CommandMap
    internal lateinit var annotation: Command

    @Throws(CommandError::class)
    internal abstract fun execute(channel: MessageChannel, message: Message, args: Array<String>)
}