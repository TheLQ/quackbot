/**
 * @(#)PluginType.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
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
package Quackbot;

import Quackbot.hook.Event;
import Quackbot.hook.HookManager;
import Quackbot.info.BotEvent;
import java.io.File;

/**
 * The core of any PluginType abstract class. All plugin types need to implement this
 * <p>
 * Many of the get* and is* methods should respond to private fields, just like a
 * JavaBean syntax. There only here to force you to use them.
 *
 *
 * @author Lord.Quackstar
 */
public interface PluginType {
	/**
	 * Should return an explanation of the command and its syntax.
	 * Verbosity should be kept to a minimum, as by default you get one line.
	 * @return And help provided
	 */
	public String getHelp();

	/**
	 * Should return true if the command is usable by Admins, false otherwise.
	 * An Admin is a privileged user able to things most can't.
	 * @return the admin
	 */
	public boolean isAdmin();

	/**
	 * Should return true if the command should be ignored, false otherwise. An
	 * ignored plugin is one that cannot be used by users and does not show up
	 * in help.
	 * @return the ignore
	 */
	public boolean isEnabled();

	/**
	 * Should return true if the plugin is a service, false otherwise. Services
	 * are plugins that are called immediately upon connection to the server and
	 * can be
	 * @return the service
	 */
	public boolean isService();

	/**
	 * Is Util?
	 * @return the util
	 */
	public boolean isUtil();

	/**
	 * Name of command
	 * @return the name
	 */
	public String getName();

	/**
	 * The file that this command parsed
	 * @return The file
	 */
	public File getFile();

	/**
	 * Change the ignore status to true or false.
	 * @param ignore Ignore?
	 */
	public void setEnabled(boolean enabled);

	public ParameterConfig getParamConfig();

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
	public void invoke(Bot bot, BotEvent msgInfo) throws Exception;

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
	public boolean load(File file) throws Exception;
}
