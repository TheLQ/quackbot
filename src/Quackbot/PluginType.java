/**
 * @(#)PluginType.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.Hooks;
import Quackbot.info.UserMessage;
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
	public boolean isUtil() ;

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
	 * Called when spefic command is requested
	 *
	 * Commands MUST override this in order to work
	 * @param bot     Bot instance
	 * @param msgInfo UserMessage bean
	 */
	public void invoke(String command, String[] args, Bot bot, UserMessage msgInfo) throws Exception;

	public void load(File file) throws Exception;

	public int getParams();

	public File getFile();
}
