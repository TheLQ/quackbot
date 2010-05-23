/**
 * @(#)Admin.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

import Quackbot.Controller;
import jpersist.Entity;
import org.slf4j.LoggerFactory;

/**
 * Bean that holds all known Admin information. This is meant to be integrated with
 * JPersist and the database. Only configure if going to add to database, otherwise let
 * JPersist configure it.
 * <p>
 * If this needs to be changed in database, call {@link #updateDB()}
 * @author admins
 */
public class Admin extends Entity {
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
	public String toString() {
		return new StringBuilder("[").append("UserName=" + getUser() + ",").append("AdminID=" + getAdminId() + ",").append("ChannelID=" + getChannelID() + ",").append("ServerID=" + getServerID()).append("]").toString();
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
	public Admin updateDB() {
		try {
			save(Controller.instance.dbm);
			return Controller.instance.dbm.loadObject(this);
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

}
