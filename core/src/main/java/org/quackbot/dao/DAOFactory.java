/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.dao;

/**
 *
 * @author Leon
 */
public interface DAOFactory {
	public AdminDAO createAdminDAO();
	public ChannelDAO createChannelDAO();
	public LogDAO createLogDAO();
	public ServerDAO createServerDAO();
	public UserDAO createUserDAO();
}
