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
import org.quackbot.dao.GenericDAO;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface UserEntry<K extends Serializable> extends GenericEntry<K> {	
	public String getNick();

	public void setNick(String nick);

	public String getLogin();

	public void setLogin(String login);

	public String getHostmask();

	public void setHostmask(String hostmask);

	public String getRealname();

	public void setRealname(String realName);

	public Integer getHops();

	public void setHops(Integer hops);

	public String getConnectedServer();

	public void setConnectedServer(String connectedServer);
}
