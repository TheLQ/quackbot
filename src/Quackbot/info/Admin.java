/**
 * @(#)Admin.java
 *
 * This file is part of Quackbot
 */
package Quackbot.info;

import jpersist.Entity;
import org.apache.commons.lang.StringUtils;

/**
 * Bean that holds all Admin info This is usually configured by JPersist
 * @author admins
 */
public class Admin extends Entity {

	/**
	 * Sterilization ID
	 */
	private static long serialVersionUID = 100L;

	/**
	 * Sterilization ID
	 * @return the serialVersionUID
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * Sterilization ID
	 * @param aSerialVersionUID the serialVersionUID to set
	 */
	public static void setSerialVersionUID(long aSerialVersionUID) {
		serialVersionUID = aSerialVersionUID;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 */
	private Integer adminId, channelID, serverID;

	/**
	 * Value mapped to column in DB or manually provided
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


	public String toString() {
		return StringUtils.join(new Object[]{getAdminId(), getChannelID(), getServerID(), getUser()}, ", ");
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the adminId
	 */
	public Integer getAdminId() {
		return adminId;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param adminId the adminId to set
	 */
	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the channelID
	 */
	public Integer getChannelID() {
		return channelID;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param channelID the channelID to set
	 */
	public void setChannelID(Integer channelID) {
		this.channelID = channelID;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the serverID
	 */
	public Integer getServerID() {
		return serverID;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param serverID the serverID to set
	 */
	public void setServerID(Integer serverID) {
		this.serverID = serverID;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Value mapped to column in DB or manually provided
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
}
