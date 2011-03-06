
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
}
