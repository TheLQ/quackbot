/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
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
package org.quackbot.dao.model;

import java.io.Serializable;

/**
 * IRC logging
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface LogEntry extends GenericEntry {
	public Long getTimestamp();

	public void setTimestamp(Long timestamp);

	public String getServer();

	public void setServer(String server);

	public String getChannel();

	public void setChannel(String channel);

	public LogEntryType getType();

	public void setType(LogEntryType type);

	public String getUser();

	public void setUser(String user);

	public String getMessage();

	public void setMessage(String message);

	public String getRawLine();

	public void setRawLine(String rawLine);
}
