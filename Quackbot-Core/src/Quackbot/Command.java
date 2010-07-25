/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import Quackbot.err.QuackbotException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
public abstract class Command {
	private String name;
	private String help;
	private boolean admin;
	private boolean enabled;
	private File file;
	private int requiredParams;
	private int optionalParams;
	private Bot bot;
	private boolean setup = false;
	private Logger log = LoggerFactory.getLogger(getClass());

	public Command() {
	}

	public Command(String name, String help, boolean admin, boolean enabled, File file, int optionalParams, int requiredParams) {
		this.name = name;
		this.help = help;
		this.admin = admin;
		this.enabled = enabled;
		this.file = file;
		this.requiredParams = requiredParams;
		this.optionalParams = optionalParams;
	}

	public Command setup(String name, String help, boolean admin, boolean enabled, File file, int optionalParams, int requiredParams) throws QuackbotException {
		if (setup)
			throw new QuackbotException("Command " + getName() + " has already been setup");
		this.name = name;
		this.help = help;
		this.admin = admin;
		this.enabled = enabled;
		this.file = file;
		this.requiredParams = requiredParams;
		this.optionalParams = optionalParams;
		setup = true;
		return this;
	}

	public Bot getBot() {
		return Bot.getPoolLocal();
	}

	/**
	 * Should return an explanation of the command and its syntax.
	 * Verbosity should be kept to a minimum, as by default you get one line.
	 * @return And help provided
	 */
	/**
	 * Should return an explanation of the command and its syntax.
	 * Verbosity should be kept to a minimum, as by default you get one line.
	 * @return And help provided
	 */
	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	/**
	 * Should return true if the command is usable by Admins, false otherwise.
	 * An Admin is a privileged user able to things most can't.
	 * @return the admin
	 */
	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * Should return true if the command should be ignored, false otherwise. An
	 * ignored plugin is one that cannot be used by users and does not show up
	 * in help.
	 * @return the ignore
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Name of command
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The file that this command parsed
	 * @return The file
	 */
	public File getFile() {
		return file;
	}

	public int getRequiredParams() {
		return requiredParams;
	}

	public int getOptionalParams() {
		return optionalParams;
	}

	public void onCommand(String channel, String sender, String login, String hostname, String[] args) throws Exception {
	}

	public void onCommandPM(String sender, String login, String hostname, String[] args) throws Exception {
	}

	public void onCommandChannel(String channel, String sender, String login, String hostname, String[] args) throws Exception {
	}
}
