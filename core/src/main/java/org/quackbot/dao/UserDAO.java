
package org.quackbot.dao;

/**
 *
 * @author lordquackstar
 */
public interface UserDAO {
	public Integer getUserId();

	public String getNick();

	public void setNick(String nick);

	public String getLogin();

	public void setLogin(String login);

	public String getHostmask();

	public void setHostmask(String hostmask);

	public String getRealname();

	public void setRealname(String realName);

	public Integer getHops();

	public void setHops(Integer hops);

	public String getConnectedServer();

	public void setConnectedServer(String connectedServer);
}
