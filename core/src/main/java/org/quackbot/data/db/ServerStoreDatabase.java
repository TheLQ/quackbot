
package org.quackbot.data.db;

import ejp.DatabaseException;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.quackbot.Controller;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.ServerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public class ServerStoreDatabase implements ServerStore {
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
	private Set<ChannelStore> channels = new HashSet<ChannelStore>();
	/**
	 * List of all Admins, refrenced by common serverID
	 */
	private Set<AdminStore> admins = new HashSet<AdminStore>();
	protected final DatabaseStore store;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Creates Server
	 * @param address Address of server
	 */
	public ServerStoreDatabase(DatabaseStore store, String address) {
		this.address = address;
		this.store = store;
	}

	/*******************************************UTILS*********************************/
	public void addAdmin(AdminStore admin) {
		loadAssociations();
		admins.add(admin);
		update();
	}

	public void removeAdmin(AdminStore admin) {
		loadAssociations();
		admins.remove(admin);
		update();
	}

	/**
	 * Add channel
	 * @param channel Channel name (must include prefix)
	 */
	public void addChannel(ChannelStore channel) {
		loadAssociations();
		channels.add(channel);
		update();
	}

	/**
	 * Removes channel
	 * @param channel Channel name (must include prefix)
	 */
	public void removeChannel(ChannelStore channel) {
		loadAssociations();
		getChannels().remove(channel);
		update();
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
	public void update() {
		//Try to save the object
		try {
			store.getDatabaseManager().saveObject(this);
		} catch (DatabaseException e) {
			log.error("Couldn't save ServerStore to database", e);
			return;
		}

		//Update ourselves with the admin ID
		try {
			ServerStoreDatabase dbVersion = store.getDatabaseManager().loadObject(this);
			setServerId(dbVersion.getServerId());
		} catch (DatabaseException ex) {
			log.error("Can't load ServerStore from database", ex);
		}
	}

	@Override
	public boolean delete() {
		try {
			store.getDatabaseManager().deleteObject(this);
			return true;
		} catch (DatabaseException ex) {
			log.error("Couldn't delete ServerStore from Database", ex);
		}
		return false;
	}
	
	public void loadAssociations() {
		try {
			store.getDatabaseManager().loadAssociations(this);
		} catch (DatabaseException ex) {
			log.error("Can't load associations from database", ex);
		}
	}
}