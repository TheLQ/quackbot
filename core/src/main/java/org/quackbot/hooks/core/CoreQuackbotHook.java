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
import org.quackbot.events.CommandEvent;
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

		//Make sure there was a prefix that got removed. No prefix = no command
		if (message.equals(event.getMessage()))
			return;

		int commandNumber = getController().addCommandNumber();
		String command = "";
		String debugSuffix = "execution of command #" + commandNumber + ",  from channel " + event.getChannel().getName() + " using message " + message;

		try {
			log.info("-----------Begin " + debugSuffix + "-----------");
			command = message.split(" ", 2)[0];
			String[] args = getArgs(message);
			Command cmd = getCommand(args, command, event.getChannel(), event.getUser());
			CommandEvent commandEvent = new CommandEvent(cmd, event, event.getChannel(), event.getUser(), event.getMessage(), command, args);
			//Send any response back to the user
			event.respond(cmd.onCommand(commandEvent));
			event.respond(executeOnCommandLong(commandEvent));
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
			String[] args = getArgs(message);
			Command cmd = getCommand(args, command, null, event.getUser());
			CommandEvent commandEvent = new CommandEvent(cmd, event, null, event.getUser(), event.getMessage(), command, args);
			event.respond(cmd.onCommand(commandEvent));
			event.respond(executeOnCommandLong(commandEvent));
		} catch (Exception e) {
			log.error("Error encountered when running command " + command, e);
			getBot().sendMessage(event.getUser(), "ERROR: " + e.getMessage());
		} finally {
			log.debug("-----------End " + debugSuffix + "-----------");
		}
	}

	public Command getCommand(String[] args, String userCommand, Channel chan, User user) throws InvalidCMDException, AdminException, NumArgException {
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

	public String executeOnCommandLong(CommandEvent commandEvent) throws Exception {
		try {
			Command command = commandEvent.getCommandClass();
			Class clazz = command.getClass();
			for (Method curMethod : clazz.getMethods())
				if (curMethod.getName().equalsIgnoreCase("onCommand") && curMethod.getParameterTypes().length != 1) {
					//Get parameters leaving off the first one
					Class[] parameters = (Class[]) ArrayUtils.remove(curMethod.getParameterTypes(), 0);

					Object[] args = new Object[command.getRequiredParams() + command.getOptionalParams()];
					String[] userArgs = commandEvent.getArgs();
					//Try and fill argument list, handling arrays
					for (int i = 0; i < args.length; i++) {
						if (parameters[i].isArray()) {
							//Look ahead to see how big of an array we need
							int arrayLength = parameters.length;
							for (int s = i; s < args.length; s++)
								if (parameters[s].isArray())
									arrayLength--;

							//Add our array of specified length
							args[i] = ArrayUtils.subarray(userArgs, i, i + arrayLength);

							//Move the index forward to account for the taken in parameters
							i += arrayLength;
						}
						else
							args[i] = userArgs[i];
					}

					//Pad the args with null values
					args = Arrays.copyOf(args, curMethod.getParameterTypes().length);

					//Prefix with command event
					args = ArrayUtils.addAll(new Object[]{commandEvent}, args);
					log.trace("Args: " + StringUtils.join(args, ","));

					//Execute method
					return (String) curMethod.invoke(command, args);
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
}
