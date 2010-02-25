/**
 * @(#)AdminCMD.java
 *
 * Admin Only Commands
 *
 * @author 
 * @version 1.00 2010/2/21
 */

package Quackbot.CMDs;

import Quackbot.*;
import Quackbot.Annotations.*;

public class AdminOnly {
	
	Quackbot qb = null;
	public String channel, sender, login, hostname, rawmsg, command;
	
	public AdminOnly(Quackbot qb) {
		this.qb = qb;
	}
	
    @HelpDoc("Locks the bot globaly. Syntax: ?lockBot <true:false>")
    public void lockBot(String Smode) {
    	System.out.println("test");
    	Boolean mode = Boolean.parseBoolean(Smode);
    	qb.botLocked = mode;
    	qb.sendMessage(channel,"Bot has been "+((mode) ? "locked" : "unlocked")+" globaly");
    }

	@HelpDoc("Locks the bot in channel. Syntax: ?lockChannel <true:false>")
	public void lockChannel(String Smode) {
		Boolean mode = Boolean.parseBoolean(Smode);
		if(mode==true) {
			qb.chanLockList.put(channel,"");
			qb.sendMessage(channel,"Bot has been locked for this channel");
		}
		else
			qb.chanLockList.remove(channel);
	}
	
	@HelpDoc("Joins a channel on current server")
	public void joinChan(String channel) {
		qb.joinChannel(channel);
		System.out.println("Joined new channel "+channel);
		qb.sendMessage(channel,sender+": Joined channel "+channel);
	}
	
	@HelpDoc("Parts a channel on current server")
	public void partChan(String channel){
		qb.partChannel(channel);
		System.out.println("Parted channel "+channel);
		qb.sendMessage(channel,sender+": Parted channel "+channel);
	}
    
}