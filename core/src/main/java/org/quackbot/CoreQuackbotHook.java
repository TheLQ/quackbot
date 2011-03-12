package org.quackbot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.hooks.events.ConnectEvent;
import org.quackbot.err.AdminException;
import org.quackbot.err.InvalidCMDException;
import org.quackbot.err.NumArgException;
import org.quackbot.hook.Hook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Owner
 */
public class CoreQuackbotHook extends Hook {
	public CoreQuackbotHook() {
		super("CoreQuackBotHooks");
	}
	private Logger log = LoggerFactory.getLogger(Bot.class);

	@Override
	public void onConnect(ConnectEvent event) {
		List<org.quackbot.data.ChannelStore> channels = getBot().serverStore.getChannels();
		for (org.quackbot.data.ChannelStore curChannel : channels) {
			getBot().joinChannel(curChannel.getName(), curChannel.getPassword());
			log.debug("Trying to join channel using " + curChannel);
		}
	}

	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		int cmdNum = getController().addCmdNum();

		String command = "";

		if (getBot().isLocked(channel, sender, true)) {
			log.warn("Bot locked");
			return;
		}

		//Look for a prefix
		for (String curPrefix : getBot().getPrefixes())
			if (curPrefix.length() < message.length() && message.substring(0, curPrefix.length()).equalsIgnoreCase(curPrefix))
				try {
					log.info("-----------Begin execution of command #" + cmdNum + ",  from channel " + channel + " using message " + message + "-----------");
					message = message.substring(curPrefix.length(), message.length()).trim();
					command = message.split(" ", 2)[0];
					BaseCommand cmd = setupCommand(command, channel, sender, login, hostname, message);
					getBot().sendMessage(channel, sender, cmd.onCommandGiven(channel, sender, login, hostname, getArgs(message)));
					getBot().sendMessage(channel, sender, cmd.onCommandChannel(channel, sender, login, hostname, getArgs(message)));
					getBot().sendMessage(channel, sender, executeOnCommand(cmd, getArgs(message)));
					break;
				} catch (Exception e) {
					log.error("Error encountered when running command " + command, e);
					getBot().sendMessage(channel, sender, "ERROR: " + e.getMessage());
				} finally {
					log.info("-----------End execution of command #" + cmdNum + ",  from channel " + channel + " using message " + message + "-----------");
				}
	}

	@Override
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		int cmdNum = getController().addCmdNum();
		log.debug("-----------Begin execution of command #" + cmdNum + ",  from a PM from " + sender + " using message " + message + "-----------");
		String command = "";

		try {
			if (getBot().isLocked(null, sender, true)) {
				log.warn("Bot locked");
				return;
			}

			//Look for a prefix
			command = message.split(" ", 2)[0];
			BaseCommand cmd = setupCommand(command, null, sender, login, hostname, message);
			getBot().sendMessage(sender, cmd.onCommandGiven(sender, sender, login, hostname, getArgs(message)));
			getBot().sendMessage(sender, cmd.onCommandPM(sender, login, hostname, getArgs(message)));
			getBot().sendMessage(sender, executeOnCommand(cmd, getArgs(message)));
		} catch (Exception e) {
			log.error("Error encountered when running command " + command, e);
			getBot().sendMessage(sender, "ERROR: " + e.getMessage());
		} finally {
			log.debug("-----------End execution of command #" + cmdNum + ",  from a PM from " + sender + " using message " + message + "-----------");
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

	public BaseCommand setupCommand(String command, String channel, String sender, String login, String hostname, String message) throws Exception {
		//Parse message to get cmd and args
		String[] args = getArgs(message);

		BaseCommand plugin = CommandManager.getCommand(command);
		//Is this a valid plugin?
		if (plugin == null || !plugin.isEnabled())
			throw new InvalidCMDException(command);
		//Is this an admin function? If so, is the person an admin?
		if (plugin.isAdmin() && !getController().isAdmin(sender, getBot(), channel))
			throw new AdminException();

		//Does the required number of args exist?
		int given = args.length;
		int required = plugin.getRequiredParams();
		int optional = plugin.getOptionalParams();
		log.debug("User Args: " + given + " | Req Args: " + required + " | Optional: " + optional);
		if (given > required + optional) //Do we have too many?
			throw new NumArgException(given, required, optional);
		else if (given < required) //Do we not have enough?
			throw new NumArgException(given, required);
		return plugin;
	}
}
