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
package Quackbot;

import Quackbot.err.QuackbotException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
public abstract class Command implements BaseCommand {
	private String name;
	private String help;
	private boolean admin;
	private boolean enabled;
	private File file;
	private int requiredParams;
	private int optionalParams;
	private boolean setup = false;
	private Logger log = LoggerFactory.getLogger(getClass());

	public Command() {
	}

	@Override
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

	@Override
	public String toString() {
		return "Name=" + name + ","
				+ "Enabled=" + enabled + ","
				+ "Admin=" + admin + ","
				+ "RequiredParams=" + requiredParams + ","
				+ "OptionalParams=" + optionalParams + ","
				+ "Help=" + help + ","
				+ "Setup=" + setup + ","
				+ "File=" + file;
	}

	@Override
	public Bot getBot() {
		return Bot.getPoolLocal();
	}

	/**
	 * Should return an explanation of the command and its syntax.
	 * Verbosity should be kept to a minimum, as by default you get one line.
	 * @return And help provided
	 */
	@Override
	public String getHelp() {
		return help;
	}

	/**
	 * Should return true if the command is usable by Admins, false otherwise.
	 * An Admin is a privileged user able to things most can't.
	 * @return the admin
	 */
	@Override
	public boolean isAdmin() {
		return admin;
	}

	@Override
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * Should return true if the command should be ignored, false otherwise. An
	 * ignored plugin is one that cannot be used by users and does not show up
	 * in help.
	 * @return the ignore
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Name of command
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * The file that this command parsed
	 * @return The file
	 */
	@Override
	public File getFile() {
		return file;
	}

	@Override
	public int getRequiredParams() {
		return requiredParams;
	}

	@Override
	public int getOptionalParams() {
		return optionalParams;
	}

	@Override
	public String onCommand() {
		return null;
	}

	@Override
	public String onCommandGiven(String channel, String sender, String login, String hostname, String[] args) throws Exception {
		return null;
	}

	@Override
	public String onCommandPM(String sender, String login, String hostname, String[] args) throws Exception {
		return null;
	}

	@Override
	public String onCommandChannel(String channel, String sender, String login, String hostname, String[] args) throws Exception {
		return null;
	}
}
