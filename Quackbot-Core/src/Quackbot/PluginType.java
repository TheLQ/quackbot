/**
 * @(#)PluginType.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.Hooks;
import Quackbot.info.BotEvent;
import java.io.File;

/**
 * The core of any PluginType abstract class. All plugin types need to implement this
 * <p>
 * Many of the get* and is* methods should respond to private fields, just like a
 * JavaBean syntax. There only here to force you to use them.
 *
 * @author Lord.Quackstar
 */
public interface PluginType {
	/**
	 * Should return the help for a command
	 * @return the help
	 */
	public String getHelp();

	/**
	 * Admin only?
	 * @return the admin
	 */
	public boolean isAdmin();

	/**
	 * Ignore command?
	 * @return the ignore
	 */
	public boolean isIgnore();

	/**
	 * Is Listener?
	 * @return the listener
	 */
	public Hooks getHook();

	/**
	 * Is server?
	 * @return the service
	 */
	public boolean isService();

	/**
	 * Is Util?
	 * @return the util
	 */
	public boolean isUtil();

	/**
	 * Requires Arguments?
	 * @return the reqArg
	 */
	public boolean isReqArg();

	/**
	 * Name of command
	 * @return the name
	 */
	public String getName();

	/**
	 * Number of parameters this command REQUIRES
	 * @return Numer of params
	 */
	public int getParams();

	/**
	 * Number of OPTIONAL parameters this command has
	 * @return Number of Optional params
	 */
	public int getOptParams();

	/**
	 * The file that this command parsed
	 * @return The file
	 */
	public File getFile();

	/**
	 * This is called when the command is requested. This is executed in a seperate thread,
	 * so the current thread can be paused without affecting bot operation.
	 * 
	 * @param args        Any arguments the user passes
	 * @param bot         The bot that sent the command. <b>Note:</b> Services should expect
	 *                    this to be null.
	 * @param msgInfo     The BotEvent that contains all message info
	 * @throws Exception  If an exception is encountered, it MUST thrown up the chain to be
	 *                    reported as an error.
	 */
	public void invoke(String[] args, Bot bot, BotEvent msgInfo) throws Exception;

	/**
	 * Load method that is called when a file that matches the extension specified
	 * is found in plugins directory. If this plugin requires manual adding (IE JavaPlugin)
	 * then it must set the name to null so it can be ignored
	 * <p>
	 * Implementations of this must completly setup the Plugin, parsing all avalible infomration.
	 * The plugin must then be able to be activated by {@link #invoke(java.lang.String[], Quackbot.Bot, Quackbot.info.BotEvent)}
	 * @param file         The file that contains the script
	 * @throws Exception   Any exception encountered while parsing. The command will not be added
	 */
	public void load(File file) throws Exception;
}
