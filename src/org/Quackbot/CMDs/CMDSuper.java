/**
 * @(#)CMDBasic.java
 *
 * CMD super class that defines basic variables
 *
 * @author  Lord.Quackstar
 */

package org.Quackbot.CMDs;

import org.Quackbot.*;

public abstract class CMDSuper {
    Bot qb = null;
	public String channel, sender, login, hostname, rawmsg, command;
	
	public void update(String channel, String sender, String login, String hostname, String rawmsg, String command,Bot bot) {
        this.channel = channel;
	    this.sender = sender;
	    this.login = login;
	    this.hostname = hostname;
	    this.rawmsg = rawmsg;
	    this.qb = bot;
	}
}