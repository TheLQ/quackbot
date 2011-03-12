package org.quackbot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.quackbot.data.ChannelStore;
import org.quackbot.err.AdminException;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.err.NumArgException;
import org.quackbot.hook.Hook;
import org.quackbot.hook.HookManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Owner
 */
public class CoreQuackbotHook extends Hook {
	private final Logger log = LoggerFactory.getLogger(Bot.class);

	@Override
	public void onConnect(ConnectEvent event) {
		Set<ChannelStore> channels = getBot().getServerStore().getChannels();
		for (org.quackbot.data.ChannelStore curChannel : channels) {
			getBot().joinChannel(curChannel.getName(), curChannel.getPassword());
			log.debug("Trying to join channel using " + curChannel);
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
		
		if(message.equals(event.getMessage()))
			//Message didn't change, meaning no prefix. Ignore
			return;
		
		int commandNumber = getController().addCommandNumber();
		String command = "";
		String debugSuffix =  "execution of command #" + commandNumber + ",  from channel " + event.getChannel().getName() + " using message " + message;
		
		try {
			log.info("-----------Begin "+debugSuffix+"-----------");
			command = message.split(" ", 2)[0];
			Command cmd = setupCommand(message, command, event.getChannel(), event.getUser());
			getBot().sendMessage(event.getChannel(), event.getUser(), cmd.onCommand(event.getChannel(), event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getChannel(), event.getUser(), cmd.onCommandChannel(event.getChannel(), event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getChannel(), event.getUser(), executeOnCommand(cmd, getArgs(message)));
		} catch (Exception e) {
			log.error("Error encountered when running command " + command, e);
			getBot().sendMessage(event.getChannel(), event.getUser(), "ERROR: " + e.getMessage());
		} finally {
			log.info("-----------End "+debugSuffix+"-----------");
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
			log.debug("-----------Begin "+debugSuffix+"-----------");
			command = message.split(" ", 2)[0];
			Command cmd = setupCommand(message, command, null, event.getUser());
			getBot().sendMessage(event.getUser(), cmd.onCommand(null, event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getUser(), cmd.onCommandPM(event.getUser(), getArgs(message)));
			getBot().sendMessage(event.getUser(), executeOnCommand(cmd, getArgs(message)));
		} catch (Exception e) {
			log.error("Error encountered when running command " + command, e);
			getBot().sendMessage(event.getUser(), "ERROR: " + e.getMessage());
		} finally {
			log.debug("-----------End "+debugSuffix+"-----------");
		}
	}

	public String executeOnCommand(BaseCommand cmd, String[] args) throws Exception {
		try {
			Class clazz = cmd.getClass();
			for (Method curMethod : clazz.getMethods())
				if (curMethod.getName().equalsIgnoreCase("onCommand") && curMethod.getReturnType().equals(String.class)) {
					//Pad the args with null values
					args = Arrays.copyOf(args, curMethod.getParameterTypes().length);
					log.trace("Args: " + StringUtils.join(args, ","));

					return (String) curMethod.invoke(cmd, (Object[]) args);
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

		Command command = HookManager.getCommand(userCommand);
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
