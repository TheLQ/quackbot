

package org.quackbot.data.db;

import ejp.DatabaseManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.quackbot.Controller;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.DataStore;
import org.quackbot.data.ServerStore;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public class DatabaseStore implements DataStore {
	protected final Controller controller;
	protected DatabaseManager databaseManager = null;
	
	public DatabaseStore(Controller controller) {
		this.controller = controller;
		//TODO: Setup Database from Config file
	}
	
	public AdminStore newAdminStore(String name) {
		return new AdminStoreDatabase(this, name);
	}

	public ChannelStore newChannelStore(String name) {
		return new ChannelStoreDatabase(this, name);
	}

	public ServerStore newServerStore(String address) {
		return new ServerStoreDatabase(this, address);
	}
	
}
