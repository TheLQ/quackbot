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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * This is the Server bean mapped to the Database by JPersist. Used by {@link Quackbot.Bot}
 *
 * This is usually configured by JPersist
 * @author Lord.Quackstar
 */
public class Server {
	/**
	 * Value mapped to column in DB or manually provided
	 */
	private String address, password;
	/**
	 * Value mapped to column in DB or manually provided
	 */
	private Integer serverId, port;
	/**
	 * List of all Channels, refrenced by common serverID
	 */
	private List<Channel> channels = new ArrayList<Channel>();
	/**
	 * List of all Admins, refrenced by common serverID
	 */
	private List<Admin> admins = new ArrayList<Admin>();

	/**
	 * Empty constructor
	 */
	public Server() {
	}

	/**
	 * Constructor specified by Server ID. Usually used to get all servers from db
	 * @param serverID
	 */
	public Server(Integer serverID) {
		this.serverId = serverID;
	}

	/**
	 * Creates Server
	 * @param address Address of server
	 */
	public Server(String address) {
		this.address = address;
	}

	/**
	 * Creates server
	 * @param address Address of server
	 * @param port    Custom port of server
	 */
	public Server(String address, Integer port) {
		this.address = address;
		this.port = port;
	}

	/**
	 * Creates server
	 * Creates server
	 * @param address  Address of server
	 * @param port     Custom port of server
	 * @param password Password of server
	 */
	public Server(String address, Integer port, String password) {
		this.address = address;
		this.port = port;
		this.password = password;
	}

	/**
	 * Creates server (<b>Warning</b> A custom ID should only be given in special circumstances
	 * @param serverId Custom server ID
	 * @param address  Address of server
	 * @param port     Custom port of server
	 * @param password Password of server
	 */
	public Server(Integer serverId, String address, Integer port, String password) {
		this.serverId = serverId;
		this.address = address;
		this.port = port;
		this.password = password;
	}

	/*******************************************UTILS*********************************/
	/**
	 * Adds admin
	 * @param admin An admin object
	 */
	public void addAdmin(Admin admin) {
		getAdmins().add(admin);
	}

	/**
	 * Removes admin
	 * @param name Name of admin
	 */
	public void removeAdmin(String name) {
		getAdmins().remove(getAdmin(name));
	}

	/**
	 * Gets admin by name
	 * @param name Name of admin
	 * @return     Admin object
	 */
	public Admin getAdmin(String name) {
		for (Admin curAdmin : getAdmins())
			if (curAdmin.getUser().equalsIgnoreCase(name))
				return curAdmin;
		return null;
	}

	/**
	 * Add channel
	 * @param channel Channel name (must include prefix)
	 */
	public void addChannel(Channel channel) {
		getChannels().add(channel);
	}

	/**
	 * Removes channel
	 * @param channel Channel name (must include prefix)
	 */
	public void removeChannel(String channel) {
		getChannels().remove(getChannel(channel));
	}

	/**
	 * Checks if channel exists
	 * @param channel Channel name (must include prefix)
	 * @return        True if found, false otherwise
	 */
	public boolean channelExists(String channel) {
		if (getChannel(channel) == null)
			return false;
		return true;
	}

	/**
	 * Gets channel object by name
	 * @param channel Channel name (must include prefix)
	 * @return        Channel object
	 */
	public Channel getChannel(String channel) {
		for (Channel curChannel : getChannels())
			if (curChannel.getName().equalsIgnoreCase(channel))
				return curChannel;
		return null;
	}

	/**
	 * Converts object to string
	 * @return String representation
	 */
	@Override
	public String toString() {
		return new StringBuilder("[").append("Address=").append(getAddress()).append(",").
				append("Password=").append(getPassword()).append(",").
				append("Port=").append(getPort()).append(",").
				append("ServerID=").append(getServerId()).append(",").
				append("Admins=").append(getAdmins().toString()).append(",").
				append("Channels=").append(getChannels().toString()).append("]").
				toString();
	}

	/**
	 * Utility to update the database with the current Server object.
	 * <p>
	 * WARNING: Passing an empty or null server object might destroy the
	 * database's knowledge of the server. Only JPersist generated Server
	 * objects should be passed
	 * <p>
	 * @return Server object with database generated info set
	 */
	public Server updateDB(Controller controller) {
		try {
			controller.getDatabase().saveObject(this);
			return controller.getDatabase().loadObject(this);
		} catch (Exception e) {
			LoggerFactory.getLogger(Server.class).error("Error updating or fetching database", e);
		}
		return null;
	}

	public int delete(Controller controller) throws DatabaseException {
		return controller.getDatabase().deleteObject(this);
	}

	/*******************************************JAVABEAN*************************/
	/**
	 * Value mapped to column in DB or manually provided
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the serverId
	 */
	public Integer getServerId() {
		return serverId;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param serverId the serverId to set
	 */
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	/**
	 * List of all Channels, refrenced by common serverID
	 * @return the channels
	 */
	public List<Channel> getChannels() {
		return channels;
	}

	/**
	 * List of all Channels, refrenced by common serverID
	 * @param channels the channels to set
	 */
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	/**
	 * List of all Admins, refrenced by common serverID
	 * @return the admins
	 */
	public List<Admin> getAdmins() {
		return admins;
	}

	/**
	 * List of all Admins, refrenced by common serverID
	 * @param admins the admins to set
	 */
	public void setAdmins(List<Admin> admins) {
		this.admins = admins;
	}
}
