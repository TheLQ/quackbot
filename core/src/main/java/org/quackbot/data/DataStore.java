
package org.quackbot.data;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface DataStore {
	public AdminStore newAdminStore(String name);
	public ChannelStore newChannelStore(String name);
	public ServerStore newServerStore(String address);
}
