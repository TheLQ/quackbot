package Quackbot.info;

import jpersist.Entity;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author admins
 */
public class Channel extends Entity {
	private static final long serialVersionUID = 100L;
	private int serverID, channelID;
	private String channel, password = null;

	public Channel() {}

	public Channel(int serverID) {
	    this.serverID = serverID;
	}

	public Channel(String channel) {
	    this.channel = channel;
	}

	public Channel(String channel, String password) {
	    this.channel = channel;
	    this.password = password;
	}
    /**
     * @return the channel
     */
    public String getChannel() {
	return channel;
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(String channel) {
	this.channel = channel;
    }

    /**
     * @return the password
     */
    public String getPassword() {
	return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
	this.password = password;
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
    public int getChannelID() {
	return channelID;
    }

    /**
     * @param channelID the channelID to set
     */
    public void setChannelID(int channelID) {
	this.channelID = channelID;
    }

    public String toString() {
	return StringUtils.join(new Object[]{channel,channelID,password,serverID},", ");
    }
}
