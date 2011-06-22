/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.hooks.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.quackbot.data.ChannelStore;
import org.quackbot.hooks.Command;
import org.quackbot.err.AdminException;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.err.NumArgException;
import org.quackbot.hooks.Hook;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class CoreQuackbotHook extends Hook {
	@Override
	public void onConnect(ConnectEvent event) {
		for (ChannelStore curChannel : getBot().getServerStore().getChannels()) {
			log.debug("Trying to join channel using " + curChannel);
			getBot().joinChannel(curChannel.getName(), curChannel.getPassword());
		}
	}

	@Override
	public void onMessage(MessageEvent event) {
		if (getBot().isLocked(event.getChannel(), event.getUser())) {
			log.warn("Bot locked");
			return;
		}

		String message = event.getMessage();

		//Look for a prefix
		for (String curPrefix : getBot().getPrefixes())
			//Strip away start of message and end when the message doesn't match anymore (meaning something changed)
			if (!(message = StringUtils.removeStartIgnoreCase(message, curPrefix)).equals(message))
				break;

		if (message.equals(event.getMessage()))
			//Message didn't change, meaning no prefix. Ignore
			return;

		int commandNumber = getController().addCommandNumber();
		String command = "";
		String debugSuffix = "execution of command #" + commandNumber + ",  from channel " + event.getChannel().getName() + " using message " + message;

		try {
			log.info("-----------Begin " + debugSuffix + "-----------");
			command = message.split(" ", 2)[0];
			Command cmd = setupCommand(message, command, event.getChannel(), event.getUser());
			getBot().sendMessage(event.getChannel(), event.getUser(), cmd.onCommand(event.getChannel(), event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getChannel(), event.getUser(), cmd.onCommandChannel(event.getChannel(), event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getChannel(), event.getUser(), executeOnCommandLong(cmd, event.getChannel(), event.getUser(), getArgs(message)));
		} catch (Exception e) {
			log.error("Error encountered when running command " + command, e);
			getBot().sendMessage(event.getChannel(), event.getUser(), "ERROR: " + e.getMessage());
		} finally {
			log.info("-----------End " + debugSuffix + "-----------");
		}
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) {
		if (getBot().isLocked(null, event.getUser())) {
			log.warn("Bot locked");
			return;
		}

		//Assume command
		int commandNumber = getController().addCommandNumber();
		String message = event.getMessage();
		String command = "";
		String debugSuffix = "execution of command #" + commandNumber + ",  from a PM from " + event.getUser() + " using message " + message;

		try {
			log.debug("-----------Begin " + debugSuffix + "-----------");
			command = message.split(" ", 2)[0];
			Command cmd = setupCommand(message, command, null, event.getUser());
			getBot().sendMessage(event.getUser(), cmd.onCommand(null, event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getUser(), cmd.onCommandPM(event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getUser(), executeOnCommandLong(cmd, null, event.getUser(), getArgs(message)));
		} catch (Exception e) {
			log.error("Error encountered when running command " + command, e);
			getBot().sendMessage(event.getUser(), "ERROR: " + e.getMessage());
		} finally {
			log.debug("-----------End " + debugSuffix + "-----------");
		}
	}

	public String executeOnCommandLong(Command cmd, Channel chan, User user, Object[] args) throws Exception {
		try {
			Class clazz = cmd.getClass();
			for (Method curMethod : clazz.getMethods())
				if (curMethod.getName().equalsIgnoreCase("onCommand") || curMethod.getName().equalsIgnoreCase("onCommandChannel")) {
					//Pad the args with null values
					args = Arrays.copyOf(args, curMethod.getParameterTypes().length);

					//Prefix with user and channel values
					args = ArrayUtils.addAll(new Object[]{chan, user}, args);
					log.trace("Args: " + StringUtils.join(args, ","));

					//Execute method
					return (String) curMethod.invoke(cmd, args);
				} else if (curMethod.getName().equalsIgnoreCase("onCommandPM")) {
					//Pad the args with null values
					args = Arrays.copyOf(args, curMethod.getParameterTypes().length);

					//Prefix with user values
					args = ArrayUtils.addAll(new Object[]{user}, args);
					log.trace("Args: " + StringUtils.join(args, ","));

					//Execute method
					return (String) curMethod.invoke(cmd, args);
				}
		} catch (InvocationTargetException e) {
			//Unrwap if nessesary
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof Exception)
				throw (Exception) e.getCause();
			throw e;
		}
		return null;
	}

	public String[] getArgs(String message) {
		message = message.trim();
		String[] args;
		if (message.contains(" "))
			args = (String[]) ArrayUtils.remove(message.split(" "), 0);
		else
			args = new String[0];
		return args;
	}

	public Command setupCommand(String message, String userCommand, Channel chan, User user) throws Exception {
		//Parse message to get cmd and args
		String[] args = getArgs(message);

		Command command = getController().getHookManager().getCommand(userCommand);
		//Is this a valid command?
		if (command == null || !command.isEnabled())
			throw new InvalidCMDException(userCommand);
		//Is this an admin function? If so, is the person an admin?
		if (command.isAdmin() && !getController().isAdmin(getBot(), user, chan))
			throw new AdminException();

		//Does the required number of args exist?
		int given = args.length;
		int required = command.getRequiredParams();
		int optional = command.getOptionalParams();
		log.debug("User Args: " + given + " | Req Args: " + required + " | Optional: " + optional);
		if (given > required + optional) //Do we have too many?
			throw new NumArgException(given, required, optional);
		else if (given < required) //Do we not have enough?
			throw new NumArgException(given, required);
		return command;
	}
}
