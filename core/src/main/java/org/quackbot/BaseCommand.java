/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot;

import org.quackbot.err.QuackbotException;
import java.io.File;

/**
 *
 * @author LordQuackstar
 */
public interface BaseCommand {
	Bot getBot();
	/**
	 * The file that this command parsed
	 * @return The file
	 */
	File getFile();

	/**
	 * Should return an explanation of the command and its syntax.
	 * Verbosity should be kept to a minimum, as by default you get one line.
	 * @return And help provided
	 */
	String getHelp();

	/**
	 * Name of command
	 * @return the name
	 */
	String getName();

	int getOptionalParams();

	int getRequiredParams();

	/**
	 * Should return true if the command is usable by Admins, false otherwise.
	 * An Admin is a privileged user able to things most can't.
	 * @return the admin
	 */
	boolean isAdmin();

	/**
	 * Should return true if the command should be ignored, false otherwise. An
	 * ignored plugin is one that cannot be used by users and does not show up
	 * in help.
	 * @return the ignore
	 */
	boolean isEnabled();

	String onCommand();
	
	String onCommandChannel(String channel, String sender, String login, String hostname, String[] args) throws Exception;

	String onCommandGiven(String channel, String sender, String login, String hostname, String[] args) throws Exception;

	String onCommandPM(String sender, String login, String hostname, String[] args) throws Exception;

	void setAdmin(boolean admin);

	void setEnabled(boolean enabled);

	Command setup(String name, String help, boolean admin, boolean enabled, File file, int optionalParams, int requiredParams) throws QuackbotException;

	@Override
	String toString();
}
