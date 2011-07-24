package org.quackbot.hooks.core;

import javax.management.RuntimeErrorException;
import org.apache.commons.lang.StringUtils;
import org.quackbot.Bot;
import org.quackbot.err.QuackbotException;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.java.AdminOnly;
import org.quackbot.hooks.java.HelpDoc;

/**
 *
 * @author lordquackstar
 */
@AdminOnly
@HelpDoc(value = "The umbrilla command for any action to a channel")
public class Channel extends Command {
	public String onCommand(CommandEvent event, String action, String[] args) throws Exception {
		Bot bot = event.getBot();

		/*** Bot status commands ***/
		if (action.equalsIgnoreCase("join")) {
			if (args.length == 0)
				throw new RuntimeException("No channel specified");
			bot.joinChannel(args[0]);
		} else if (action.equalsIgnoreCase("part"))
			if (args.length == 0)
				//No args, assume current channel
				bot.partChannel(event.getChannel());
			else {
				//Want to part other channel; make sure it exists
				if (!bot.channelExists(args[0]))
					throw new RuntimeException("Channel " + args[0] + " doesn't exist");
				bot.partChannel(bot.getChannel(args[0]));
			}
		else if (action.equalsIgnoreCase("quit"))
			if (args.length == 0)
				//No args = no quit message
				bot.quitServer();
			else
				//Args, join to make quit message
				bot.quitServer(StringUtils.join(args, " "));
		/**** Set user channel status commands *****/
		else if (action.equalsIgnoreCase("op")) {
			userArgCheck(event, args);
			event.getChannel().op(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("voice")) {
			userArgCheck(event, args);
			event.getChannel().voice(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("halfOp")) {
			userArgCheck(event, args);
			event.getChannel().halfOp(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("superOp")) {
			userArgCheck(event, args);
			event.getChannel().superOp(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("owner")) {
			userArgCheck(event, args);
			event.getChannel().owner(bot.getUser(args[0]));
		} /*** Remove user channel status commands ***/
		else if (action.equalsIgnoreCase("deop")) {
			userArgCheck(event, args);
			event.getChannel().deOp(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("devoice")) {
			userArgCheck(event, args);
			event.getChannel().deVoice(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("dehalfOp")) {
			userArgCheck(event, args);
			event.getChannel().deHalfOp(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("desuperOp")) {
			userArgCheck(event, args);
			event.getChannel().deSuperOp(bot.getUser(args[0]));
		} else if (action.equalsIgnoreCase("deowner")) {
			userArgCheck(event, args);
			event.getChannel().deOwner(bot.getUser(args[0]));
		} /*** Other user commands ***/
		else if (action.equalsIgnoreCase("ban") || action.equalsIgnoreCase("kickban"))
			if (args[0].contains("!"))
				//Contains a !, assume hostmask
				bot.ban(event.getChannel(), args[0]);
			else {
				//Must be a user
				userArgCheck(event, args);
				bot.ban(event.getChannel(), "*!*@" + bot.getUser(args[0]).getHostmask());
			}
		else if (action.equalsIgnoreCase("unban"))
			if (args[0].contains("!"))
				//Contains a !, assume hostmask
				bot.unBan(event.getChannel(), args[0]);
			else {
				//Must be a user
				userArgCheck(event, args);
				bot.unBan(event.getChannel(), "*!*@" + bot.getUser(args[0]).getHostmask());
			}
		else if (action.equalsIgnoreCase("tempBan")) {
			if(args.length != 2)
				throw new RuntimeException("Not enough arguments. Should be <hostmask or user> <time in minuites>");
			int banTime = Integer.parseInt(args[1]);
			String hostmask;
			if (args[0].contains("!"))
				//Contains a !, assume hostmask
				hostmask = args[0];
			else {
				//Must be a user
				userArgCheck(event, args);
				hostmask = "*!*@" + bot.getUser(args[0]).getHostmask();
			}
			bot.ban(event.getChannel(), hostmask);
			Thread.sleep(banTime * 60 * 1000);
			bot.unBan(event.getChannel(), hostmask);
		} else if (action.equalsIgnoreCase("kick") || action.equalsIgnoreCase("kickban")) {
			userArgCheck(event, args);
			bot.kick(event.getChannel(), bot.getUser(args[0]));
		} else
			throw new RuntimeException("Unknown operation: " + action);
		return null;
	}

	protected void userArgCheck(CommandEvent event, String[] args) {
		Bot bot = event.getBot();
		if (args.length == 0)
			throw new RuntimeException("No user specified");
		if (bot.userExists(args[0]))
			throw new RuntimeException("User " + args[0] + " doesn't exist");
		if (!event.getChannel().getUsers().contains(bot.getUser(args[0])))
			throw new RuntimeException("User " + args[0] + " doesn't exist this channel (" + event.getChannel().getName() + ")");
	}
}
