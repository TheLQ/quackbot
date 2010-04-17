package Quackbot.info;

import jpersist.Entity;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author admins
 */
public class Admin extends Entity {

	private static final long serialVersionUID = 100L;
	private int adminId, channelID, serverID;
	private String user;

	public Admin() {}

	public Admin(String name) {
	    this.user = name;
	}

    /**
     * @return the serverID
     */
    public int getServerID() {
	return serverID;
    }

    /**
     * @param serverID the serverID to set
     */
    public void setServerID(int serverID) {
	this.serverID = serverID;
    }

    /**
     * @return the channelID
     */
    public Integer getChannelID() {
	return channelID;
    }

    /**
     * @param channelID the channelID to set
     */
    public void setChannelID(Integer channelID) {
	this.channelID = channelID;
    }

    /**
     * @return the user
     */
    public String getUser() {
	return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
	this.user = user;
    }

    /**
     * @return the adminId
     */
    public int getAdminId() {
	return adminId;
    }

    /**
     * @param adminId the adminId to set
     */
    public void setAdminId(int adminId) {
	this.adminId = adminId;
    }

    public String toString() {
	return StringUtils.join(new Object[]{adminId,channelID,serverID,user},", ");
    }
}
