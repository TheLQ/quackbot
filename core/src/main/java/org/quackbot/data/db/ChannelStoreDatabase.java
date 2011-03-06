

package org.quackbot.data.db;

import ejp.DatabaseException;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public class ChannelStoreDatabase implements ChannelStore {
	/**
	 * ID of the server Channel is attached to
	 */
	private Integer serverID;
	/**
	 * ID of channel in Database
	 */
	private Integer channelID;
	/**
	 * Name of the channel
	 */
	private String name;
	/**
	 * Password of the channel. Can be null.
	 */
	private String password;
	protected DatabaseStore store;
	private Set<AdminStore> admins = new HashSet<AdminStore>();
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Create from string
	 * @param name
	 */
	public ChannelStoreDatabase(DatabaseStore store, String name) {
		this.name = name;
		this.store = store;
	}

	/**
	 * Utility to update the database with the current Admin object.
	 * @return Channel object with database generated info set
	 */
	public void update() {
		//Try to save the object
		try {
			store.getDatabaseManager().saveObject(this);
		} catch (DatabaseException e) {
			log.error("Couldn't save ChannelStore to database", e);
			return;
		}

		//Update ourselves with the admin ID
		try {
			ChannelStoreDatabase dbVersion = store.getDatabaseManager().loadObject(this);
			setChannelID(dbVersion.getChannelID());
		} catch (DatabaseException ex) {
			log.error("Can't load ChannelStore from database", ex);
		}
	}
	
	public void loadAssociations() {
		try {
			store.getDatabaseManager().loadAssociations(this);
		} catch (DatabaseException ex) {
			log.error("Can't load associations from database", ex);
		}
	}

	public boolean delete() {
		try {
			store.getDatabaseManager().deleteObject(this);
			return true;
		} catch (DatabaseException ex) {
			log.error("Couldn't delete ChannelStore from Database", ex);
		}
		return false;
	}

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
}