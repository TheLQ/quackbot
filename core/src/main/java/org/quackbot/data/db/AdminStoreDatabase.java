package org.quackbot.data.db;

import ejp.DatabaseException;
import java.util.Set;
import lombok.Data;
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
public class AdminStoreDatabase implements AdminStore {
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
	private Set<ChannelStore> channels;
	private Set<ServerStore> servers;
	protected final DatabaseStore store;
	/**
	 * Logging system
	 */
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Generate from name
	 * @param name  Name of admin
	 */
	public AdminStoreDatabase(DatabaseStore store, String name) {
		this.user = name;
		this.store = store;
	}

	@Override
	public boolean delete() {
		try {
			store.getDatabaseManager().deleteObject(this);
			return true;
		} catch (DatabaseException ex) {
			log.error("Couldn't delete AdminStore from Database", ex);
		}
		return false;
	}

	public void update() {
		//Try to save the object
		try {
			store.getDatabaseManager().saveObject(this);
		} catch (DatabaseException e) {
			log.error("Couldn't save AdminStore to database", e);
			return;
		}

		//Update ourselves with the admin ID
		try {
			AdminStore dbVersion = store.getDatabaseManager().loadObject(this);
			setChannelID(dbVersion.getChannelID());
		} catch (DatabaseException ex) {
			log.error("Can't load AdminStore from database", ex);
		}
	}
	
	public void loadAssociations() {
		try {
			store.getDatabaseManager().loadAssociations(this);
		} catch (DatabaseException ex) {
			log.error("Can't load associations from database", ex);
		}
	}

	public Set<ChannelStore> getChannels() {
		loadAssociations();
		return channels;
	}

	public Set<ServerStore> getServers() {
		loadAssociations();
		return servers;
	}
}
