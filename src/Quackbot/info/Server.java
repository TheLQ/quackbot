/**
 * @(#)Server.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

import java.util.ArrayList;
import java.util.List;

import jpersist.PersistentObject;
import jpersist.annotations.UpdateNullValues;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This is the Server bean mapped to the Database by JPersist. Used by {@link Quackbot.Bot}
 *
 * This is usually configured by JPersist
 * @author Lord.Quackstar
 */
@UpdateNullValues
public class Server extends PersistentObject {

	private static long serialVersionUID = 100L;

	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * @param aSerialVersionUID the serialVersionUID to set
	 */
	public static void setSerialVersionUID(long aSerialVersionUID) {
		serialVersionUID = aSerialVersionUID;
	}
	/**
	 * Value mapped to column in DB or manually provided
	 */
	private String address, password, port;
	/**
	 * Value mapped to column in DB or manually provided
	 */
	private int serverId;
	/**
	 * List of all Channels, refrenced by common serverID
	 */
	private List<Channel> channels = new ArrayList<Channel>();
	/**
	 * List of all Admins, refrenced by common serverID
	 */
	private List<Admin> admins = new ArrayList<Admin>();
	/**
	 * Log4j Logger
	 */
	private Logger log = Logger.getLogger(Server.class);

	/**
	 * Empty constructor
	 */
	public Server() {
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
	public Server(String address, String port) {
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
	public Server(String address, String port, String password) {
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
	public Server(int serverId, String address, String port, String password) {
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
	 * Checks if admin exists
	 * @param name Name of admin
	 * @return     True if found, false otherwise
	 */
	public boolean adminExists(String name) {
		if (getAdmin(name) == null)
			return false;
		return true;
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
	 * Gets channel ojbect by name
	 * @param channel Channel name (must include prefix)
	 * @return        Channel object
	 */
	public Channel getChannel(String channel) {
		for (Channel curChannel : getChannels())
			if (curChannel.getChannel().equalsIgnoreCase(channel))
				return curChannel;
		return null;
	}

	/**
	 * Converts object to string
	 * @return String representation
	 */
	public String toString() {
		return StringUtils.join(new Object[]{getAddress(), getPassword(), getPort(), getServerId(), getAdmins(), getChannels()}, ", ");
	}

	/*******************************************ASSOSIATIONS*************************/
	/**
	 * Note: This is only for JPersist framework. DO NOT CALL THIS
	 * @param c Channel object
	 * @return  List of channel objects
	 */
	public List<Channel> getDbAssociation(Channel c) {
		getLog().debug("Called get DB association - chan");
		return getChannels();
	}

	/**
	 * Note: This is only for JPersist framework. DO NOT CALL THIS
	 * @param c
	 * @param s
	 */
	public void setDbAssociation(Channel c, List<Channel> s) {
		getLog().debug("Called set DB association - chan");
		setChannels(s);
	}

	/**
	 * Note: This is only for JPersist framework. DO NOT CALL THIS
	 * @param c
	 * @return DB associations
	 */
	public List<Admin> getDbAssociation(Admin c) {
		getLog().debug("Called get DB association - admin");
		return getAdmins();
	}

	/**
	 *
	 * Note: This is only for JPersist framework. DO NOT CALL THIS
	 * @param c
	 * @param o
	 */
	public void setDbAssociation(Admin c, List<Admin> o) {
		getLog().debug("Called set DB association - admin");
		setAdmins(o);
	}

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
	public String getPort() {
		return port;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the serverId
	 */
	public int getServerId() {
		return serverId;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param serverId the serverId to set
	 */
	public void setServerId(int serverId) {
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

	/**
	 * Log4j Logger
	 * @return the log
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * Log4j Logger
	 * @param log the log to set
	 */
	public void setLog(Logger log) {
		this.log = log;
	}
}
