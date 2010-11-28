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
package Quackbot.info;

import Quackbot.Controller;
import ejp.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean that holds all known Admin information. This is meant to be integrated with
 * JPersist and the database. Only configure if going to add to database, otherwise let
 * JPersist configure it.
 * <p>
 * If this needs to be changed in database, call {@link #updateDB()}
 * @author admins
 */
public class Admin {
	/**
	 * The ID of the admin
	 */
	private Integer adminId;
	/**
	 * The ID of the channel the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates this isn't attached to a channel. The admin can
	 * either be server admin or global admin
	 */
	private Integer channelID;
	/**
	 * The ID of the server the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates the admin isn't attached to a server. They must
	 * be a global admin.
	 */
	private Integer serverID;
	/**
	 * The username of the admin
	 */
	private String user;
	/**
	 * Logging system
	 */
	private static Logger log = LoggerFactory.getLogger(Admin.class);

	/**
	 * Empty constructor
	 */
	public Admin() {
	}

	/**
	 * Generate from name
	 * @param name  Name of admin
	 */
	public Admin(String name) {
		this.user = name;
	}

	/**
	 * Converts admin to String representation
	 * @return String representation
	 */
	@Override
	public String toString() {
		return new StringBuilder("[").append("UserName=").append(getUser()).append(",").
				append("AdminID=").append(getAdminId()).append(",").
				append("ChannelID=").append(getChannelID()).append(",").
				append("ServerID=").append(getServerID()).append("]").
				toString();
	}

	/**
	 * Utility to update the database with the current Admin object.
	 * <p>
	 * WARNING: Passing an empty or null server object might destroy the
	 * database's knowledge of the server. Only JPersist generated Server
	 * objects should be passed
	 * <p>
	 * This is a convience method for
	 * <br>
	 * <code>try {
	 *		save(Controller.instance.dbm);
	 * catch (Exception e) {
	 * updating database", e);
	 *	}</code>
	 * @return Admin object with database generated info set
	 */
	public Admin updateDB(Controller controller) {
		try {
			controller.config.getDatabase().saveObject(this);
			return controller.config.getDatabase().loadObject(this);
		} catch (Exception e) {
			LoggerFactory.getLogger(Server.class).error("Error updating or fetching database", e);
		}
		return null;
	}

	/**
	 * The ID of the admin
	 * @return the adminId
	 */
	public Integer getAdminId() {
		return adminId;
	}

	/**
	 * The ID of the admin
	 * @param adminId the adminId to set
	 */
	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	/**
	 * The ID of the channel the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates this isn't attached to a channel. The admin can
	 * either be server admin or global admin
	 * @return the channelID
	 */
	public Integer getChannelID() {
		return channelID;
	}

	/**
	 * The ID of the channel the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates this isn't attached to a channel. The admin can
	 * either be server admin or global admin
	 * @param channelID the channelID to set
	 */
	public void setChannelID(Integer channelID) {
		this.channelID = channelID;
	}

	/**
	 * The ID of the server the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates the admin isn't attached to a server. They must
	 * be a global admin.
	 * @return the serverID
	 */
	public Integer getServerID() {
		return serverID;
	}

	/**
	 * The ID of the server the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates the admin isn't attached to a server. They must
	 * be a global admin.
	 * @param serverID the serverID to set
	 */
	public void setServerID(Integer serverID) {
		this.serverID = serverID;
	}

	/**
	 * The username of the admin
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * The username of the admin
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * The Channel object that the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates this isn't attached to a channel. The admin can
	 * either be server admin or global admin
	 * <p>
	 * Note that this isn't mapped by JPersist, it is simply a convience method
	 * @return the channel
	 */
	public Channel getChannel(Controller controller) {
		try {
			return controller.getDatabase().loadObject(new Channel(getChannelID()));
		} catch (DatabaseException e) {
			log.error("Could not fetch channel", e);
		}
		return null;
	}

	/**
	 * The Server object that the admin might be attached to. Can be null
	 * <p>
	 * A null value indicates the admin isn't attached to a server. They must
	 * be a global admin.
	 * <p>
	 * Note that this isn't mapped by JPersist, it is simply a convience method
	 * @return the server
	 */
	public Server getServer(Controller controller) {
		try {
			return controller.getDatabase().loadObject(new Server(getChannelID()));
		} catch (DatabaseException e) {
			log.error("Could not fetch Server", e);
		}
		return null;
	}
}
