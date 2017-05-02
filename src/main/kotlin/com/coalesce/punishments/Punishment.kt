package com.coalesce.punishments

import com.coalesce.Bot
import com.sun.security.auth.callback.TextCallbackHandler
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Punishment(val reason: Reason, val punisher: User, val by: String, val description: String?) {
    val json = JSONObject()

    init {
        json.put("type", "automatic").put("reason", reason.toString()).put("by", by)
        if(description != null) json.put("description", description)
    }

    fun doActUpon(pastPunishments: JSONArray, user: User, channel: MessageChannel): JSONArray {
        var amount = 0
        var severity = 0

        pastPunishments.iterator().forEach {
            if(it is JSONObject){
                amount ++

                var reason = Reason.valueOf(it.getString("reason"))
                severity += reason.severity
            }
        }

        severity = Math.min(Math.max(severity, 1), PunishmentTimes.values().size) //Severity is in range: 1-7
        /*
            1: Warn
            2: Second Warning
            3: Mute for 1 hour
            4: Mute for 8 hours
            5: Mute for 2 days
            6: Mute for a week
            7: Permanently Mute
        */

        createPunishment(severity, amount, user, channel)
        pastPunishments.put(json)

        return pastPunishments
    }

    fun message(user: User, channel: MessageChannel, embedBuilder: EmbedBuilder, warning: Boolean, untilString: String?, amount: Int) {

        if(warning) embedBuilder.setTitle("⚠ You have received a warning for breaking the rules. ⚠", null)
        else embedBuilder.setTitle("🚫 You have been muted for breaking the rules. 🚫", null)

        val message = StringBuilder()

        message.append("Reason: " + reason.description + "\n")
        if(description != null) message.append("Description: $description\n")
        if(untilString != null) message.append("Until: $untilString\n")
        message.append("Please refer to <#269178364483338250> before chatting.\n")
        message.append("Repeat offense of the rules will lead to harsher punishments.\n")
        message.append("You have $amount punishments in record.")

        embedBuilder.setDescription(message.toString())
        
        val msg = MessageBuilder().setEmbed(embedBuilder.build()).append(user.asMention).build()
        channel.sendMessage(msg).queue()

        val serverLogChannel = Bot.instance.jda.getTextChannelById("299385639437074433")
        val serverLogEmbedBuilder = EmbedBuilder()

        serverLogEmbedBuilder.setAuthor(by, null, punisher.avatarUrl)
        serverLogEmbedBuilder.setColor(Color(255, 64, 64))

        var punishmentShort = "Warning"
        if(!warning)
            if(untilString == null) punishmentShort = "Muted permanently"
            else punishmentShort = "Muted until $untilString"
        serverLogEmbedBuilder.addField("Punishment", punishmentShort, true)
        serverLogEmbedBuilder.addField("Reason", reason.description, true)

        serverLogEmbedBuilder.setTimestamp(msg.creationTime)

        try {
            val serverLogMsg = MessageBuilder().setEmbed(serverLogEmbedBuilder.build()).build()
            serverLogChannel.sendMessage(serverLogMsg).queue()
        } catch (e: Exception) {}

    }

    fun createPunishment(severity: Int, amount: Int, user: User, channel: MessageChannel) {
        val embedBuilder = EmbedBuilder()
        val punishmentTime = PunishmentTimes.values()[severity]

        if (punishmentTime.timeUnit < 0) {
            message(user, channel, embedBuilder, true, null, amount)
        } else {
            //Muting
            val guild = (channel as TextChannel).guild
            val member = guild.getMember(user)
            val role = guild.getRoleById("303317692608282625")
            guild.controller.addRolesToMember(member, role).queue()

            //Create Mute Message
            var untilString : String? = null
            if(punishmentTime.timeUnit != 0) {
                val calendar = Calendar.getInstance()
                calendar.add(punishmentTime.timeUnit, punishmentTime.time)
                untilString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.time)
                json.put("until", calendar.timeInMillis)

                val executorService = Executors.newScheduledThreadPool(1)
                executorService.schedule(PTimerTask(guild, member, role, executorService), System.currentTimeMillis() - calendar.timeInMillis,
                        TimeUnit.MILLISECONDS)
            }

            message(user, channel, embedBuilder, false, untilString, amount)
        }
    }
}