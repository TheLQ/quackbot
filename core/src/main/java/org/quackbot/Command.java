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
package org.quackbot;

import java.io.File;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.quackbot.hook.Hook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Command extends Hook {
	private String help;
	private boolean admin;
	private boolean enabled;
	private int requiredParams;
	private int optionalParams;
	private boolean setup = false;
	private Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Create a Command with the given name. File is null
	 * @param name The name to use for this Command
	 */
	public Command(String name) {
		super(name);
	}

	/**
	 * Create a Command with the given file and name
	 * @param file The file that this Command came from
	 * @param name The name to use for this Command
	 */
	public Command(File file, String name) {
		super(file, name);
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
