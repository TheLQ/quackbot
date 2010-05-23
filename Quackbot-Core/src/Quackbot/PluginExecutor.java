/**
 * @(#)PluginExecutor.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.err.AdminException;
import Quackbot.err.InvalidCMDException;
import Quackbot.err.NumArgException;

import Quackbot.info.BotMessage;
import Quackbot.info.BotEvent;


import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All calls to ANY command run are called using this class.
 * <p>
 * Using given command, will execute the appropiate plugin by calling its plugintype's invoke method
 * <p>
 * This is meant to be executed inside of either a Bot threadpool or Main threadpool
 * @author Lord.Quackstar
 */
public class PluginExecutor implements Runnable {
	/**
	 * Name of command to be run
	 */
	private String command;
	/**
	 * Arguments to pass
	 */
	private String[] params;
	/**
	 * Bot instance (optional)
	 */
	private Bot bot;
	/**
	 * BotEvent bean (used if Bot is executing a command)
	 */
	private BotEvent msgInfo;
	/**
	 * Current Controller instance
	 */
	private Controller ctrl = Controller.instance;
	/**
	 * Log4j logger
	 */
	private Logger log = LoggerFactory.getLogger(PluginExecutor.class);

	/**
	 * Constructs PluginExecutor for bot. Will report errors to passed bot
	 * @param bot     Bot making the call
	 * @param msgInfo BotEvent bean generated by bot
	 */
	public PluginExecutor(Bot bot, BotEvent msgInfo) {
		this.command = msgInfo.getCommand();
		this.params = msgInfo.getArgs();
		this.msgInfo = msgInfo;
		this.bot = bot;
	}

	/**
	 * Constructs PluginExecutor for anything else. Errors reported to GUI
	 * @param command Command wished to run
	 * @param params  Parameters needed for commands
	 */
	public PluginExecutor(String command, String[] params) {
		this.command = command;
		this.params = params;
		this.bot = null;
		this.msgInfo = null;
	}

	/**
	 * In new thread, does all checking and execution of specified command
	 */
	public void run() {
		msgInfo.setCmdNum(ctrl.addCmdNum());

		log.info("-----------Begin execution of command #" + msgInfo.getCmdNum() + ",  from " + msgInfo.getRawmsg() + "-----------");
		try {
			PluginType plugin = ctrl.findPlugin(command);
			if (plugin == null || plugin.isService() || plugin.isUtil())
				throw new InvalidCMDException(command);
			//Is this an admin function? If so, is the person an admin?
			if (plugin.isAdmin() && bot != null && !bot.serverDB.adminExists(msgInfo.getSender()))
				throw new AdminException();

			//Does this method require args?
			if (plugin.isReqArg() && params.length == 0) {
				log.debug("Method does require args, passing length 1 array");
				params = new String[1];
			}

			//Does the required number of args exist?
			int paramLen = params.length;
			int paramNum = plugin.getOptParams();
			int reqParamNum = plugin.getParams();
			log.debug("User Args: " + paramLen + " | Req Args: " + reqParamNum + " | Optional: " + paramNum);
			if (paramLen > paramNum) //Do we have too many?
				throw new NumArgException(paramLen, reqParamNum, paramNum - reqParamNum);
			else if (paramLen < reqParamNum) //Do we not have enough?
				throw new NumArgException(paramLen, reqParamNum);

			//All requirements are met, excecute method
			log.info("All tests passed, running method " + command);

			plugin.invoke(params, bot, msgInfo);
		} catch (AdminException e) {
			log.error("Person is not admin!!", e);
			sendIfBot(new BotMessage(msgInfo, e));
		} catch (NumArgException e) {
			log.error("Wrong params!!!", e);
			sendIfBot(new BotMessage(msgInfo, e));
		} catch (InvalidCMDException e) {
			log.error("Command does not exist!", e);
			sendIfBot(new BotMessage(msgInfo, e));
		} catch (ClassCastException e) {
			if (StringUtils.contains(e.getMessage(), "BasePlugin")) {
				log.error("Can't cast java plugin to BasePlugin (maybe class isn't exentding it?) of " + command, e);
				sendIfBot(new BotMessage(msgInfo, new ClassCastException("Can't cast java plugin to BasePlugin (maybe class isn't exentding it?)")));
			} else {
				log.error("Other classCastException in plugin " + command, e);
				sendIfBot(new BotMessage(msgInfo, e));
			}
		} catch (Exception e) {
			log.error("Other error in plugin execution of " + command, e);
			sendIfBot(new BotMessage(msgInfo, e));
		}
		log.info("-----------End execution of command #" + msgInfo.getCmdNum() + ",  from " + msgInfo.getRawmsg() + "-----------");
	}

	/**
	 * Utility to send message to server only if  isn't null
	 * @param msg Message to send
	 */
	private void sendIfBot(BotMessage msg) {
		if (bot != null)
			bot.sendMessage(msg.channel, msg.toString());
	}
}
