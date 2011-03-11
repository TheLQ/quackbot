
package org.quackbot.data;

import java.util.Set;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface DataStore {
	public AdminStore newAdminStore(String name);
	public ChannelStore newChannelStore(String name);
	public ServerStore newServerStore(String address);
	public Set<ServerStore> getServers();
	/**
	 * Get all global, server, and channel admins. This must NOT return duplicate
	 * entries. Implmentations should weed out duplicates by checking admin ID's
	 * @return 
	 */
	public Set<AdminStore> getAllAdmins();
}
