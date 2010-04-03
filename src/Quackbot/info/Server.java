package Quackbot.info;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class Server {
	@Field(path = true) private String path = "/servers";
	@Field 	            private String address = "";
	@Collection         private List<Channel> channels = new ArrayList<Channel>();
	@Collection         private List<Admin> admins = new ArrayList<Admin>();

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
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
	 * @return All channels
	 */
	public List<Channel> getChannels() {
		return channels;
	}

	

	/**
	 * Add channel to channel array
	 * @param channel
	 */
	public void addChannel(Channel channel) {
		this.channels.add(channel);
	}

	/**
	 * Remove specified channel from array
	 * @param channel
	 */
	public void removeChannel(String channel) {
		//loop over all channels until right one is found
		Iterator chanItr = this.channels.iterator();
		while(chanItr.hasNext()) {
			Channel curChan = (Channel)chanItr.next();
			if(curChan.getName().equals(channel))
				this.channels.remove(curChan);
		}
	}

	/**
	 * @param channels the channels to set
	 */
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	/**
	 * Removes all channels from channel array
	 */
	public void removeAllChannels() {
		this.setChannels(new ArrayList<Channel>());
	}

	/**
	 * @return All Admins
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

	/**
	 * Append admin to admins array
	 * @param name
	 */
	public void addAdmin(Admin admin) {
		this.admins.add(admin);
	}

	/**
	 * Remove admin from admins array
	 * @param name
	 */
	public void removeAdmin(String name) {
		//loop over all admins until right one is found
		Iterator chanItr = this.admins.iterator();
		while(chanItr.hasNext()) {
			Admin curAdmin = (Admin)chanItr.next();
			if(curAdmin.getName().equals(name))
				this.admins.remove(curAdmin);
		}
	}

	/**
	 * Remove all admins from admins array
	 */
	public void removeAllAdmins() {
		this.setAdmins(new ArrayList<Admin>());
	}
}
