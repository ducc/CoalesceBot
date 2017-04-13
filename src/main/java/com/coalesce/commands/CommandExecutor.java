package com.coalesce.commands;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public abstract class CommandExecutor {
    protected JDA jda;
    
    protected abstract void execute(MessageChannel channel, Message message, String[] args)
            throws Exception;

    protected Command getAnnotation() {
        return getClass().getAnnotation(Command.class);
    }
}
