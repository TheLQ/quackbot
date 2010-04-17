package Quackbot.info;

import java.util.ArrayList;
import java.util.List;
import jpersist.PersistentObject;
import jpersist.annotations.Table;
import org.apache.commons.lang.StringUtils;

//@UpdateNullValues
public class Server extends PersistentObject {

    private static final long serialVersionUID = 100L;
    private String address, password, port;
    private int serverId;
    private List<Channel> channels = new ArrayList<Channel>();
    private List<Admin> admins = new ArrayList<Admin>();

    public Server() {
    }

    public Server(String address) {
	this.address = address;
    }

    public Server(String address, String port) {
	this.address = address;
	this.port = port;
    }

    public Server(String address, String port, String password) {
	this.address = address;
	this.port = port;
	this.password = password;
    }

    public Server(int serverId, String address, String port, String password) {
	this.serverId = serverId;
	this.address = address;
	this.port = port;
	this.password = password;
    }

    /*******************************************UTILS*********************************/
    public void addAdmin(Admin admin) {
	getAdmins().add(admin);
    }

    public void removeAdmin(String name) {
	getAdmins().remove(getAdmin(name));
    }

    public boolean AdminExists(String name) {
	if (getAdmin(name) == null) {
	    return false;
	}
	return true;
    }

    public Admin getAdmin(String name) {
	for (Admin curAdmin : getAdmins()) {
	    if (curAdmin.getUser().equalsIgnoreCase(name)) {
		return curAdmin;
	    }
	}
	return null;
    }

    public void addChannel(Channel channel) {
	getChannels().add(channel);
    }

    public void removeChannel(String channel) {
	getChannels().remove(getChannel(channel));
    }

    public boolean channelExists(String channel) {
	if (getChannel(channel) == null) {
	    return false;
	}
	return true;
    }

    public Channel getChannel(String channel) {
	for (Channel curChannel : getChannels()) {
	    if (curChannel.getChannel().equalsIgnoreCase(channel)) {
		return curChannel;
	    }
	}
	return null;
    }

    public String toString() {
	return StringUtils.join(new Object[]{address, password, port, serverId, admins, channels}, ", ");
    }

    /*******************************************ASSOSIATIONS*************************/
    public List<Channel> getDbAssociation(Channel c) {
	System.out.println("Called get DB association - chan");
	return getChannels();
    }

    public void setDbAssociation(Channel c, List<Channel> s) {
	System.out.println("Called set DB association - chan");
	setChannels(s);
    }

    public List<Admin> getDbAssociation(Admin c) {
	System.out.println("Called get DB association - admin");
	return getAdmins();
    }

    public void setDbAssociation(Admin c, List<Admin> o) {
	System.out.println("Called set DB association - admin");
	setAdmins(o);
    }

    /**
     * @return the address
     */
    public String getAddress() {
	return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
	this.address = address;
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
     * @return the serverId
     */
    public int getServerId() {
	return serverId;
    }

    /**
     * @param serverId the serverId to set
     */
    public void setServerId(int serverId) {
	this.serverId = serverId;
    }

    /**
     * @return the port
     */
    public String getPort() {
	return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
	this.port = port;
    }

    /**
     * @return the channels
     */
    public List<Channel> getChannels() {
	return channels;
    }

    /**
     * @param channels the channels to set
     */
    public void setChannels(List<Channel> channels) {
	this.channels = channels;
    }

    /**
     * @return the admins
     */
    public List<Admin> getAdmins() {
	return admins;
    }

    /**
     * @param admins the admins to set
     */
    public void setAdmins(List<Admin> admins) {
	this.admins = admins;
    }
}
