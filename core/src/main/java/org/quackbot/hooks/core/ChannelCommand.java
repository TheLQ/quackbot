package org.quackbot.hooks.core;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.quackbot.Bot;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.java.AdminOnly;
import org.quackbot.hooks.java.HelpDoc;
import org.quackbot.hooks.java.Optional;

/**
 *
 * @author lordquackstar
 */
@AdminOnly
@HelpDoc("The umbrilla command for any action to a channel")
public class ChannelCommand extends Command {
	public String onCommand(CommandEvent event, String action, String target, @Optional String[] arg2) throws Exception {
		Bot bot = event.getBot();

		/*** Bot status commands ***/
		if (action.equalsIgnoreCase("join")) {
			if (StringUtils.isBlank(target))
				throw new RuntimeException("No channel specified");
			bot.joinChannel(target);
		} else if (action.equalsIgnoreCase("part"))
			if (StringUtils.isBlank(target))
				//No args, assume current channel
				bot.partChannel(event.getChannel());
			else {
				//Want to part other channel; make sure it exists
				if (!bot.channelExists(target))
					throw new RuntimeException("Channel " + target + " doesn't exist");
				bot.partChannel(bot.getChannel(target));
			}
		/*** Ignore commands ***/
		else if (action.equalsIgnoreCase("ignore"))
			if (target.equalsIgnoreCase("list")) {
				ArrayList<String> channelNames = new ArrayList(bot.getIgnoredChannels().size());
				for (Channel curChannel : bot.getIgnoredChannels())
					channelNames.add(curChannel.getName());
				ArrayList<String> userNames = new ArrayList(bot.getIgnoredUsers().size());
				for (User curUser : bot.getIgnoredUsers())
					userNames.add(curUser.getNick());
				return "Ignored channels: " + channelNames + " | Ignored users: " + userNames;
			} else if (target.equalsIgnoreCase("add")) {
				if(bot.userExists(arg2[0])) {
					bot.getIgnoredUsers().add(bot.getUser(arg2[0]));
					return "Ignoring user " + arg2[0];
				} else if (bot.channelExists(arg2[0])) {
					bot.getIgnoredChannels().add(bot.getChannel(arg2[0]));
					return "Ignoring channel " + arg2[0];
				} else
					throw new RuntimeException("Unknown channel or user " + arg2[0]);
			} else if (target.equalsIgnoreCase("remove")) {
				if(bot.userExists(arg2[0])) {
					bot.getIgnoredUsers().remove(bot.getUser(arg2[0]));
					return "Unignoring user " + arg2[0];
				} else if (bot.channelExists(arg2[0])) {
					bot.getIgnoredChannels().remove(bot.getChannel(arg2[0]));
					return "Unignoring channel " + arg2[0];
				} else
					throw new RuntimeException("Unknown channel or user " + arg2[0]);
			} else
				throw new RuntimeException("Unknown ignore operation: " + target);
		/**** Set user channel status commands *****/
		else if (action.equalsIgnoreCase("op")) {
			userArgCheck(event, target);
			event.getChannel().op(bot.getUser(target));
		} else if (action.equalsIgnoreCase("voice")) {
			userArgCheck(event, target);
			event.getChannel().voice(bot.getUser(target));
		} else if (action.equalsIgnoreCase("halfOp")) {
			userArgCheck(event, target);
			event.getChannel().halfOp(bot.getUser(target));
		} else if (action.equalsIgnoreCase("superOp")) {
			userArgCheck(event, target);
			event.getChannel().superOp(bot.getUser(target));
		} else if (action.equalsIgnoreCase("owner")) {
			userArgCheck(event, target);
			event.getChannel().owner(bot.getUser(target));
		} /*** Remove user channel status commands ***/
		else if (action.equalsIgnoreCase("deop")) {
			userArgCheck(event, target);
			event.getChannel().deOp(bot.getUser(target));
		} else if (action.equalsIgnoreCase("devoice")) {
			userArgCheck(event, target);
			event.getChannel().deVoice(bot.getUser(target));
		} else if (action.equalsIgnoreCase("dehalfOp")) {
			userArgCheck(event, target);
			event.getChannel().deHalfOp(bot.getUser(target));
		} else if (action.equalsIgnoreCase("desuperOp")) {
			userArgCheck(event, target);
			event.getChannel().deSuperOp(bot.getUser(target));
		} else if (action.equalsIgnoreCase("deowner")) {
			userArgCheck(event, target);
			event.getChannel().deOwner(bot.getUser(target));
		} /*** Other user commands ***/
		else if (action.equalsIgnoreCase("ban") || action.equalsIgnoreCase("kickban"))
			if (target.contains("!"))
				//Contains a !, assume hostmask
				bot.ban(event.getChannel(), target);
			else {
				//Must be a user
				userArgCheck(event, target);
				bot.ban(event.getChannel(), "*!*@" + bot.getUser(target).getHostmask());
			}
		else if (action.equalsIgnoreCase("unban"))
			if (target.contains("!"))
				//Contains a !, assume hostmask
				bot.unBan(event.getChannel(), target);
			else {
				//Must be a user
				userArgCheck(event, target);
				bot.unBan(event.getChannel(), "*!*@" + bot.getUser(target).getHostmask());
			}
		else if (action.equalsIgnoreCase("tempBan")) {
			if (arg2.length != 1)
				throw new RuntimeException("Not enough arguments. Should be <hostmask or user> <time in minuites>");
			int banTime = Integer.parseInt(arg2[0]);
			String hostmask;
			if (target.contains("!"))
				//Contains a !, assume hostmask
				hostmask = target;
			else {
				//Must be a user
				userArgCheck(event, target);
				hostmask = "*!*@" + bot.getUser(target).getHostmask();
			}
			bot.ban(event.getChannel(), hostmask);
			Thread.sleep(banTime * 60 * 1000);
			bot.unBan(event.getChannel(), hostmask);
		} else if (action.equalsIgnoreCase("kick") || action.equalsIgnoreCase("kickban")) {
			userArgCheck(event, target);
			bot.kick(event.getChannel(), bot.getUser(target));
		} else
			throw new RuntimeException("Unknown operation: " + action);
		return null;
	}

	protected void userArgCheck(CommandEvent event, String user) {
		Bot bot = event.getBot();
		if (StringUtils.isBlank(user))
			throw new RuntimeException("No user specified");
		if (bot.userExists(user))
			throw new RuntimeException("User " + user + " doesn't exist");
		if (!event.getChannel().getUsers().contains(bot.getUser(user)))
			throw new RuntimeException("User " + user + " doesn't exist this channel (" + event.getChannel().getName() + ")");
	}
}
