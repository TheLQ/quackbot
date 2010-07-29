/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.plugins;

import Quackbot.Command;
import Quackbot.CommandManager;

import Quackbot.PluginLoader;
import Quackbot.err.QuackbotException;
import Quackbot.hook.Hook;
import Quackbot.hook.HookManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.ReplyConstants;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JS utility bean, holds all information about JS plugin
 * @author Lord.Quackstar
 */
public class JSPluginLoader implements PluginLoader {
	/**
	 * Log4j Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JSPluginLoader.class);

	@Override
	public void load(File file) throws Exception {
		//Basic setup

		/**
		 * Compiled script (for faster execution)
		 */
		String name = StringUtils.split(file.getName(), ".")[0];
		log.debug("New JavaScript Plugin: " + name);

		//Read File Line By Line
		BufferedReader input = new BufferedReader(new FileReader(file));
		StringBuilder fileContents = new StringBuilder();
		fileContents.append("importPackage(Packages.Quackbot);");
		fileContents.append("importPackage(Packages.Quackbot.info);");
		fileContents.append("importPackage(Packages.Quackbot.hook);");
		fileContents.append("importPackage(Packages.Quackbot.err);");
		fileContents.append("importClass(Packages.java.lang.Thread);");
		String strLine;
		while ((strLine = input.readLine()) != null)
			fileContents.append(strLine).append(System.getProperty("line.separator"));
		input.close();

		//Make an Engine and a context to use for this plugin
		final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		final Bindings scope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		final Invocable inv = (Invocable) jsEngine;
		jsEngine.eval(genQuackUtils());
		jsEngine.eval(fileContents.toString());

		//Should we just ignore this?
		if (castToBoolean(scope.get("ignore"))) {
			log.info("Ignore variable set, skipping");
			return;
		}

		//Add the QuackUtils js utility class
		Object quackUtils = scope.get("QuackUtils");

		//Is this a hook?

		for (String curFunction : scope.keySet())
			if (HookManager.getNames().contains(curFunction))
				//It contains a hook method, assume that the whole thing is a hook
				//HookManager.addPluginHook(((Invocable) jsEngine).getInterface(BaseHook.class));
				return;

		if (scope.get("params") != null)
			throw new QuackbotException("Should use up to date params");

		//Fill in cmd Info
		boolean admin = castToBoolean(scope.get("admin"));
		boolean enabled = castToBoolean(scope.get("enabled"));
		boolean util = castToBoolean(scope.get("util"));
		String help = (String) scope.get("help");
		String src = fileContents.toString();

		int optionalParams = ((Double) inv.invokeMethod(quackUtils, "getOptionalParams")).intValue();
		int requiredParams = ((Double) inv.invokeMethod(quackUtils, "getRequiredParams")).intValue();

		scope.put("log", LoggerFactory.getLogger("Quackbot.plugins.js." + name));
		scope.put("file", file);
		scope.put("name", name);

		CommandManager.addCommand(new JSCommand(name, help, admin, enabled, file, requiredParams, optionalParams, jsEngine, util, src));
	}

	public static boolean castToBoolean(Object obj) {
		if (obj == null || !(obj instanceof Boolean))
			return false;
		return (Boolean) obj;
	}

	public String genQuackUtils() {
		return "var util = false;\n"
				+ "var hook = null;\n"
				+ "var ignore = false;\n"
				+ "var parameters = 0;\n"
				+ "var admin = false;\n"
				+ "var enabled = true;\n"
				+ "var help = '';\n"
				+ "var QuackUtils = {\n"
				+ "	toJavaArray: function(type, arr) {\n"
				+ "		var jArr;\n"
				+ "		if(arr.length) {\n"
				+ "			jArr = java.lang.reflect.Array.newInstance(type, arr.length);\n"
				+ "			for(var i=0;i<arr.length;i++)\n"
				+ "				jArr[i] = arr[i];\n"
				+ "		}\n"
				+ "		else {\n"
				+ "			jArr = java.lang.reflect.Array.newInstance(type, 1);\n"
				+ "			jArr[0] = arr;\n"
				+ "		}\n"
				+ "		return jArr;\n"
				+ "	},\n"
				+ "	getRequiredParams: function() {\n"
				+ "		//Is it even set?\n"
				+ "		if(typeof parameters == 'undefined')\n"
				+ "			return 0;\n"
				+ "		else if(typeof(parameters) == 'object')\n"
				+ "			if(QuackUtils.isArray(parameters))\n"
				+ "				return parameters[0]\n"
				+ "			else if(typeof parameters.required == 'undefined')\n"
				+ "				return 0;\n"
				+ "			else //Required field must be a number\n"
				+ "				return parameters.required;\n"
				+ "		//Must be a number\n"
				+ "		else\n"
				+ "			return parameters;\n"
				+ "	},\n"
				+ "	getOptionalParams: function() {\n"
				+ "		if(typeof parameters == 'undefined')\n"
				+ "			return 0;\n"
				+ "		else if(typeof(parameters) == 'object')\n"
				+ "			if(QuackUtils.isArray(parameters))\n"
				+ "				return parameters[1]\n"
				+ "			else if(typeof parameters.optional== 'undefined')\n"
				+ "				return 0;\n"
				+ "			else //Required field must be a string or a number\n"
				+ "				return parameters.optional;\n"
				+ "		//Must be a number, but only for required\n"
				+ "		else\n"
				+ "			return 0;\n"
				+ "	},\n"
				+ "	isArray: function(object) {\n"
				+ "		return object.length && typeof object != 'string';\n"
				+ "	},\n"
				+ "	pickBest: function(param, defult) {\n"
				+ "		if(typeof param == 'undefined')\n"
				+ "			return defult;\n"
				+ "		else\n"
				+ "			return param;\n"
				+ "	},\n"
				+ "	stringClass: new java.lang.String().getClass()\n"
				+ "}\n"
				+ "function getEnabled() {\n"
				+ "	return command.getEnabled();\n"
				+ "}\n"
				+ "function getBot() {\n"
				+ "	return Bot.getThreadLocal();\n"
				+ "}\n"
				+ "function setEnabled(value) {\n"
				+ "	return command.setEnabled(value);\n"
				+ "}\n"
				+ "function getAdmin() {\n"
				+ "	return command.getAdmin();\n"
				+ "}\n"
				+ "function setAdmin(value) {\n"
				+ "	return command.setAdmin(value);\n"
				+ "}\n"
				+ "function getHelp() {\n"
				+ "	return command.getHelp();\n"
				+ "}\n"
				+ "function setHelp(value) {\n"
				+ "	return command.setHelp(value);\n"
				+ "}\n"
				+ "function getEnabled() {\n"
				+ "	return command.getEnabled();\n"
				+ "}";
	}

	private static class JSCommand extends Command {
		private ScriptEngine jsEngine = null;
		private final boolean util;
		private final String src;
		private final Bindings scope;
		public static CompiledScript compiled;
		private final String help = "";

		public JSCommand(String name, String help, boolean admin, boolean enabled, File file, int optionalParams, int requiredParams, ScriptEngine jsEngine, boolean util, String src) {
			try {
				setup(name, help, admin, enabled, file, optionalParams, requiredParams);
			} catch (QuackbotException e) {
				log.error("Could not create JSCommand " + name);
			}
			this.jsEngine = jsEngine;
			this.util = util;
			this.src = src;
			this.scope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		}

		@Override
		public void onCommandGiven(String channel, String sender, String login, String hostname, String[] args) throws Exception {
			Object[] baseArgs = new Object[]{channel, sender, login, hostname};
			if (jsEngine.eval("typeof(onCommand)").equals("function"))
				if (castToBoolean(scope.get("expandArgs")))
					handle().invokeFunction("onCommand", ArrayUtils.addAll(baseArgs, args));
				else
					handle().invokeFunction("onCommand", ArrayUtils.add(baseArgs, args));
		}

		@Override
		public void onCommandChannel(String channel, String sender, String login, String hostname, String[] args) throws Exception {
			Object[] baseArgs = new Object[]{channel, sender, login, hostname};
			if (jsEngine.eval("typeof(onCommandChannel)").equals("function"))
				if (castToBoolean(scope.get("expandArgs")))
					handle().invokeFunction("onCommandChannel", ArrayUtils.addAll(baseArgs, args));
				else
					handle().invokeFunction("onCommandChannel", ArrayUtils.add(baseArgs, args));
		}

		@Override
		public void onCommandPM(String sender, String login, String hostname, String[] args) throws Exception {
			Object[] baseArgs = new Object[]{sender, login, hostname};
			if (jsEngine.eval("typeof(onCommandPM)").equals("function"))
				if (castToBoolean(scope.get("expandArgs")))
					handle().invokeFunction("onCommandPM", ArrayUtils.addAll(baseArgs, args));
				else
					handle().invokeFunction("onCommandPM", ArrayUtils.add(baseArgs, args));
		}

		protected Invocable handle() throws Exception {
			if (compiled == null) {
				//Get all utils to add to command header
				StringBuilder compSrc = new StringBuilder();
				for (Command curCmd : CommandManager.getCommands())
					if (curCmd instanceof JSCommand && ((JSCommand)curCmd).util)
						compSrc.append(src);
				compiled = ((Compilable) jsEngine).compile(compSrc + src);
			}
			scope.put("log", LoggerFactory.getLogger("Quackbot.plugins.js." + getName()));
			scope.put("command", this);
			compiled.eval(scope);
			return (Invocable) compiled.getEngine();
		}
	}

	protected static class JSHook extends Hook {
		public ScriptEngine jsEngine;

		public JSHook(ScriptEngine jsEngine) {
			this.jsEngine = jsEngine;
		}

		protected void handle(String name, Object... args) throws ScriptException, NoSuchMethodException {
			//First, does this hook function exist?
			Object func = jsEngine.get(name);
			if (func == null)
				return;

			//Exists, so just execute it
			((Invocable) jsEngine).invokeFunction(name, args);
		}

		/**
		 * This method is called whenever a message is sent to a channel.
		 *
		 * @param channel The channel to which the message was sent.
		 * @param sender The nick of the person who sent the message.
		 * @param login The login of the person who sent the message.
		 * @param hostname The hostname of the person who sent the message.
		 * @param message The actual message sent to the channel.
		 */
		@Override
		public void onMessage(String channel, String sender, String login, String hostname, String message) throws Exception {
			handle("onMessage", channel, sender, login, hostname, message);
		}

		/**
		 * This method is called whenever a private message is sent to the PircBot.
		 *
		 * @param sender The nick of the person who sent the private message.
		 * @param login The login of the person who sent the private message.
		 * @param hostname The hostname of the person who sent the private message.
		 * @param message The actual message.
		 */
		@Override
		public void onPrivateMessage(String sender, String login, String hostname, String message) throws Exception {
			handle("onPrivateMessage", sender, login, hostname, message);
		}

		/**
		 * See {@link Event#onDisconnect}
		 */
		@Override
		public void onDisconnect() throws Exception {
			handle("onDisconnect");
		}

		/**
		 * See {@link Event#onServerResponse}
		 *
		 * @param code The three-digit numerical code for the response. Stored as BotEvent.extra
		 * @param response The full response from the IRC server. Stored as BotEvent.rawmsg
		 *
		 * @see ReplyConstants
		 */
		@Override
		public void onServerResponse(int code, String response) throws Exception {
			handle("onServerResponse", code, response);
		}

		/**
		 * See {@link Event#onUserList}
		 *
		 * @param channel The name of the channel. Stored as BotEvent.rawmsg
		 * @param users An array of User objects belonging to this channel. Stored as BotEvent.extra
		 *
		 * @see User
		 */
		@Override
		public void onUserList(String channel, User[] users) throws Exception {
			handle("onUserList", channel, users);
		}

		/**
		 * See {@link Event#onAction}
		 *
		 * @param sender The nick of the user that sent the action.
		 * @param login The login of the user that sent the action.
		 * @param hostname The hostname of the user that sent the action.
		 * @param target The target of the action, be it a channel or our nick.
		 * @param action The action carried out by the user.
		 */
		@Override
		public void onAction(String sender, String login, String hostname, String target, String action) throws Exception {
			handle("onAction", sender, login, hostname, target, action);
		}

		/**
		 * See {@link Event#onNotice}
		 *
		 * @param sourceNick The nick of the user that sent the notice.
		 * @param sourceLogin The login of the user that sent the notice.
		 * @param sourceHostname The hostname of the user that sent the notice.
		 * @param target The target of the notice, be it our nick or a channel name.
		 * @param notice The notice message.
		 */
		@Override
		public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) throws Exception {
			handle("onNotice", sourceNick, sourceLogin, sourceHostname, target, notice);
		}

		/**
		 * See {@link Event#onJoin}
		 *
		 * @param channel The channel which somebody joined.
		 * @param sender The nick of the user who joined the channel.
		 * @param login The login of the user who joined the channel.
		 * @param hostname The hostname of the user who joined the channel.
		 */
		@Override
		public void onJoin(String channel, String sender, String login, String hostname) throws Exception {
			handle("onJoin", channel, sender, login, hostname);
		}

		/**
		 * See {@link Event#onPart}
		 *
		 * @param channel The channel which somebody parted from.
		 * @param sender The nick of the user who parted from the channel.
		 * @param login The login of the user who parted from the channel.
		 * @param hostname The hostname of the user who parted from the channel.
		 */
		@Override
		public void onPart(String channel, String sender, String login, String hostname) throws Exception {
			handle("onPart", channel, sender, login, hostname);
		}

		/**
		 * See {@link Event#onNickChange}
		 *
		 * @param oldNick The old nick.
		 * @param login The login of the user.
		 * @param hostname The hostname of the user.
		 * @param newNick The new nick.
		 */
		@Override
		public void onNickChange(String oldNick, String login, String hostname, String newNick) throws Exception {
			handle("onNickChange", oldNick, login, hostname, newNick);
		}

		/**
		 * See {@link Event#onKick}
		 *
		 * @param channel The channel from which the recipient was kicked.
		 * @param kickerNick The nick of the user who performed the kick. Stored as BotEvent.sender
		 * @param kickerLogin The login of the user who performed the kick. Stored as BotEvent.login
		 * @param kickerHostname The hostname of the user who performed the kick. Stored as BotEvent.hostName
		 * @param recipientNick The unfortunate recipient of the kick. Stored as BotEvent.extra
		 * @param reason The reason given by the user who performed the kick. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) throws Exception {
			handle("onKick", channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
		}

		/**
		 * See {@link Event#onQuit}
		 *
		 * @param sourceNick The nick of the user that quit from the server.
		 * @param sourceLogin The login of the user that quit from the server.
		 * @param sourceHostname The hostname of the user that quit from the server.
		 * @param reason The reason given for quitting the server.
		 */
		@Override
		public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) throws Exception {
			handle("onQuit", sourceNick, sourceLogin, sourceHostname, reason);
		}

		/**
		 * See {@link Event#onTopic}
		 *
		 * @param channel The channel that the topic belongs to.
		 * @param topic The topic for the channel. Stored as BotEvent.rawmsg
		 * @param setBy The nick of the user that set the topic. Stored as BotEvent.sender
		 * @param date When the topic was set (milliseconds since the epoch). Stored as BotEvent.extra
		 * @param changed True if the topic has just been changed, false if
		 *                the topic was already there. Stored as BotEvent.extra1
		 *
		 */
		@Override
		public void onTopic(String channel, String topic, String setBy, long date, boolean changed) throws Exception {
			handle("onTopic", channel, topic, setBy, date, changed);
		}

		/**
		 * See {@link Event#onChannelInfo}
		 *
		 * @param channel The name of the channel.
		 * @param userCount The number of users visible in this channel. Stored as BotEvent.extra
		 * @param topic The topic for this channel. Stored as BotEvent.rawmsg
		 *
		 * @see #listChannels() listChannels
		 */
		@Override
		public void onChannelInfo(String channel, int userCount, String topic) throws Exception {
			handle("onChannelInfo", channel, userCount, topic);
		}

		/**
		 * See {@link Event#onMode}
		 *
		 * @param channel The channel that the mode operation applies to.
		 * @param sourceNick The nick of the user that set the mode.
		 * @param sourceLogin The login of the user that set the mode.
		 * @param sourceHostname The hostname of the user that set the mode.
		 * @param mode The mode that has been set. Stored as BotEvent.rawmsg
		 *
		 */
		@Override
		public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
			handle("onMode", channel, sourceNick, sourceLogin, sourceHostname, mode);
		}

		/**
		 * See {@link Event#onUserMode}
		 *
		 * @param targetNick The nick that the mode operation applies to.
		 * @param sourceNick The nick of the user that set the mode.
		 * @param sourceLogin The login of the user that set the mode.
		 * @param sourceHostname The hostname of the user that set the mode.
		 * @param mode The mode that has been set. Stored as BotEvent.rawmsg
		 *
		 */
		@Override
		public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
			handle("onUserMode", targetNick, sourceNick, sourceLogin, sourceHostname, mode);
		}

		/**
		 * See {@link Event#onOp}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param recipient The nick of the user that got 'opped'. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
			handle("onOp", channel, sourceNick, sourceLogin, sourceHostname, recipient);
		}

		/**
		 * See {@link Event#onDeop}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param recipient The nick of the user that got 'deopped'. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
			handle("onDeop", channel, sourceNick, sourceLogin, sourceHostname, recipient);
		}

		/**
		 * See {@link Event#onVoice}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param recipient The nick of the user that got 'voiced'. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
			handle("onVoice", channel, sourceNick, sourceLogin, sourceHostname, recipient);
		}

		/**
		 * See {@link Event#onDeVoice}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param recipient The nick of the user that got 'devoiced'. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
			handle("onDeVoice", channel, sourceNick, sourceLogin, sourceHostname, recipient);
		}

		/**
		 * See {@link Event#onSetChannelKey}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param key The new key for the channel. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
			handle("onSetChannelKey", channel, sourceNick, sourceLogin, sourceHostname, key);
		}

		/**
		 * See {@link Event#onRemoveChannelKey}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param key The key that was in use before the channel key was removed. Stored as BotEvent.key
		 */
		@Override
		public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
			handle("onRemoveChannelKey", channel, sourceNick, sourceLogin, sourceHostname, key);
		}

		/**
		 * See {@link Event#onSetChannelLimit}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param limit The maximum number of users that may be in this channel at the same time. Stored as BotEvent.extra
		 */
		@Override
		public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) throws Exception {
			handle("onSetChannelLimit", channel, sourceNick, sourceLogin, sourceHostname, limit);
		}

		/**
		 * See {@link Event#onRemoveChannelLimit}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onRemoveChannelLimit", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onSetChannelBan}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param hostmask The hostmask of the user that has been banned. Stored  as BotEvent.rawmsg
		 */
		@Override
		public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
			handle("onSetChannelBan", channel, sourceNick, sourceLogin, sourceHostname, hostmask);
		}

		/**
		 * See {@link Event#onRemoveChannelBan}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 * @param hostmask The hostmask of the user that has been banned. Stored  as BotEvent.rawmsg
		 */
		@Override
		public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
			handle("onRemoveChannelBan", channel, sourceNick, sourceLogin, sourceHostname, hostmask);
		}

		/**
		 * See {@link Event#onSetTopicProtection}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onSetTopicProtection", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onRemoveTopicProtection}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onRemoveTopicProtection", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onSetNoExternalMessages}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onSetNoExternalMessages", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onRemoveNoExternalMessages}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onRemoveNoExternalMessages", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onSetInviteOnly}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onSetInviteOnly", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onRemoveInviteOnly}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onRemoveInviteOnly", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onSetModerated}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onSetModerated", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onRemoveModerated}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onRemoveModerated", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onSetPrivate}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onSetPrivate", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onRemovePrivate}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onRemovePrivate", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onSetSecret}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onSetSecret", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onRemoveSecret}
		 *
		 * @param channel The channel in which the mode change took place.
		 * @param sourceNick The nick of the user that performed the mode change.
		 * @param sourceLogin The login of the user that performed the mode change.
		 * @param sourceHostname The hostname of the user that performed the mode change.
		 */
		@Override
		public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
			handle("onRemoveSecret", channel, sourceNick, sourceLogin, sourceHostname);
		}

		/**
		 * See {@link Event#onInvite}
		 *
		 * @param targetNick The nick of the user being invited - should be us!
		 * @param sourceNick The nick of the user that sent the invitation.
		 * @param sourceLogin The login of the user that sent the invitation.
		 * @param sourceHostname The hostname of the user that sent the invitation.
		 * @param channel The channel that we're being invited to. Stored as rawmsg
		 */
		@Override
		public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) throws Exception {
			handle("onInvite", targetNick, sourceNick, sourceLogin, sourceHostname, channel);
		}

		/**
		 * See {@link Event#onIncomingFileTransfer}
		 *
		 * @param transfer The DcccFileTransfer that you may accept. Stored as BotEvent.extra
		 *
		 * @see DccFileTransfer
		 *
		 */
		@Override
		public void onIncomingFileTransfer(DccFileTransfer transfer) throws Exception {
			handle("onIncomingFileTransfer", transfer);
		}

		/**
		 * See {@link Event#onFileTransferFinished}
		 *
		 * @param transfer The DccFileTransfer that has finished. Stored as BotEvent.extra
		 * @param e null if the file was transfered successfully, otherwise this
		 *          will report what went wrong. Stored as BotEvent.extra1
		 *
		 * @see DccFileTransfer
		 *
		 */
		@Override
		public void onFileTransferFinished(DccFileTransfer transfer, Exception e) throws Exception {
			handle("onFileTransferFinished", transfer, e);
		}

		/**
		 * See {@link Event#onIncomingChatRequest}
		 *
		 * @param chat A DccChat object that represents the incoming chat request. Stored as BotEvent.extra
		 *
		 * @see DccChat
		 *
		 */
		@Override
		public void onIncomingChatRequest(DccChat chat) throws Exception {
			handle("onIncomingChatRequest", chat);
		}

		/**
		 * See {@link Event#onVersion}
		 *
		 * @param sourceNick The nick of the user that sent the VERSION request.
		 * @param sourceLogin The login of the user that sent the VERSION request.
		 * @param sourceHostname The hostname of the user that sent the VERSION request.
		 * @param target The target of the VERSION request, be it our nick or a channel name.
		 */
		@Override
		public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
			handle("onVersion", sourceNick, sourceLogin, sourceHostname, target);
		}

		/**
		 * See {@link Event#onPing}
		 *
		 * @param sourceNick The nick of the user that sent the PING request.
		 * @param sourceLogin The login of the user that sent the PING request.
		 * @param sourceHostname The hostname of the user that sent the PING request.
		 * @param target The target of the PING request, be it our nick or a channel name.
		 * @param pingValue The value that was supplied as an argument to the PING command. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) throws Exception {
			handle("onPing", sourceNick, sourceLogin, sourceHostname, target, pingValue);
		}

		/**
		 * See {@link Event#onServerPing}
		 *
		 * @param response The response that should be given back in your PONG. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onServerPing(String response) throws Exception {
			handle("onServerPing", response);
		}

		/**
		 * See {@link Event#onTime}
		 *
		 * @param sourceNick The nick of the user that sent the TIME request.
		 * @param sourceLogin The login of the user that sent the TIME request.
		 * @param sourceHostname The hostname of the user that sent the TIME request.
		 * @param target The target of the TIME request, be it our nick or a channel name.
		 */
		@Override
		public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
			handle("onTime", sourceNick, sourceLogin, sourceHostname, target);
		}

		/**
		 * See {@link Event#onFinger}
		 *
		 * @param sourceNick The nick of the user that sent the FINGER request.
		 * @param sourceLogin The login of the user that sent the FINGER request.
		 * @param sourceHostname The hostname of the user that sent the FINGER request.
		 * @param target The target of the FINGER request, be it our nick or a channel name.
		 */
		@Override
		public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
			handle("onFinger", sourceNick, sourceLogin, sourceHostname, target);
		}

		/**
		 * See {@link Event#onUnknown}
		 *
		 * @param line The raw line that was received from the server. Stored as BotEvent.rawmsg
		 */
		@Override
		public void onUnknown(String line) throws Exception {
			handle("onUnknown", line);
		}
	}
}
