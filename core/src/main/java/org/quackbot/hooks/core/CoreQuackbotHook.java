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
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.hooks.Command;
import org.quackbot.err.AdminException;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.err.NumArgException;
import org.quackbot.err.QuackbotException;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.QListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Component
@Slf4j
public class CoreQuackbotHook extends QListener {
	@Override
	public void onConnect(ConnectEvent event) {
		for (ChannelEntry curChannel : getBot(event).getServerEntry().getChannels()) {
			log.debug("Trying to join channel using " + curChannel);
			event.getBot().joinChannel(curChannel.getName(), curChannel.getPassword());
		}
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		String message = event.getMessage();

		//Look for a prefix
		for (String curPrefix : getBot(event).getPrefixes())
			//Strip away start of message and end when the message doesn't match anymore (meaning something changed)
			if (!(message = StringUtils.removeStartIgnoreCase(message, curPrefix)).equals(message))
				break;

		//Make sure there was a prefix that got removed. No prefix = no command
		if (message.equals(event.getMessage())) {
			log.trace("Ignoring message, no prefix");
			return;
		}

		execute(event, event.getChannel(), event.getUser(), message);
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
		execute(event, null, event.getUser(), event.getMessage());
	}

	protected void execute(Event event, Channel chan, User user, String message) throws Exception {
		if (getBot(event).isIgnored(chan, user)) {
			log.warn("Bot locked");
			return;
		}

		int commandNumber = getController().addCommandNumber();
		String commandText = "";
		String fromText = (chan != null) ? "channel " + chan.getName() : " a PM by " + user.getNick();
		String debugSuffix = "execution of command #" + commandNumber + ",  from " + fromText + " using message " + message;

		try {
			log.info("-----------Begin " + debugSuffix + "-----------");
			commandText = message.split(" ", 2)[0];
			String[] args = getArgs(message);

			//Load command
			Command command = getController().getHookManager().getCommand(commandText);
			//Is this a valid command?
			if (command == null || !command.isEnabled())
				throw new InvalidCMDException(commandText);
			//Is this an admin function? If so, is the person an admin?
			if (command.isAdmin() && !getController().isAdmin(getBot(event), user, chan))
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

			//Generate a CommandEvent that's passed to all onCommand listeners
			CommandEvent commandEvent = new CommandEvent(command, event, chan, user, message, commandText, args);
			//Send any response back to the user
			event.respond(command.onCommand(commandEvent));
			event.respond(executeOnCommandLong(commandEvent));
		} catch (Exception e) {
			getBot(event).sendMessage(chan, user, "ERROR: " + e.getMessage());
			throw new QuackbotException("Error encountered when running command " + commandText, e);
		} finally {
			log.info("-----------End " + debugSuffix + "-----------");
		}
	}

	protected String executeOnCommandLong(CommandEvent commandEvent) throws Exception {
		try {
			Command command = commandEvent.getCommandClass();
			Class clazz = command.getClass();
			for (Method curMethod : clazz.getMethods())
				if (curMethod.getName().equalsIgnoreCase("onCommand") && curMethod.getParameterTypes().length != 1) {
					//Get parameters leaving off the first one
					Class[] parameters = (Class[]) ArrayUtils.remove(curMethod.getParameterTypes(), 0);

					Object[] args = new Object[0];
					String[] userArgs = commandEvent.getArgs();
					log.debug("UserArgs: " + StringUtils.join(userArgs, ", "));
					//Try and fill argument list, handling arrays
					for (int i = 0; i < userArgs.length; i++) {
						log.trace("Current parameter: " + i);
						if (i < parameters.length && parameters[i].isArray()) {
							log.trace("Parameter " + i + " is an array");
							//Look ahead to see how big of an array we need
							int arrayLength = parameters.length - (i - 1);
							for (int s = i; s < parameters.length; s++)
								if (!parameters[s].isArray())
									arrayLength--;

							//Add our array of specified length
							log.trace("Parameter " + i + " is an array. Assigning it the next " + arrayLength + " args");
							Object[] curArray = ArrayUtils.subarray(userArgs, i, i + arrayLength);
							args = ArrayUtils.add(args, curArray);
							log.trace("Parameter " + i + " set to " + StringUtils.join(curArray, ", "));

							//Move the index forward to account for the args folded into the array
							i += arrayLength - 1;
						} else {
							log.trace("User arg " + i + " isn't an array, assigning " + userArgs[i]);
							args = ArrayUtils.add(args, userArgs[i]);
						}
					}

					//Pad the args with null values
					args = Arrays.copyOf(args, parameters.length);

					//Prefix with command event
					args = ArrayUtils.addAll(new Object[]{commandEvent}, args);
					log.trace("Args minus CommandEvent: " + StringUtils.join(ArrayUtils.remove(args, 0), ", "));

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
