/**
 * @(#)Admin.java
 *
 * Admin Only Command list
 *
 * @author Lord.Quackstar
 */

package org.Quackbot.CMDs;

import org.Quackbot.*;
import org.Quackbot.Annotations.*;

public class Admin extends CMDSuper {
	
	@AdminOnly
    @HelpDoc("Locks the bot globaly. Syntax: ?lockBot")
    public void lockBot() {
    	qb.botLocked = true;
    	qb.sendMessage(channel,"Bot has been locked globaly");
    }
	
	@AdminOnly
    @HelpDoc("Locks the bot globaly. Syntax: ?unlockBot")
    public void unlockBot() {
    	qb.botLocked = false;
    	qb.sendMessage(channel,"Bot has been unlocked globaly");
    }
	
	@AdminOnly
	@HelpDoc("Locks the bot in channel. Syntax: ?lockChannel")
	public void lockChannel() {
		qb.chanLockList.put(channel,"");
		qb.sendMessage(channel,"Bot has been locked for this channel");
	}
	
	@AdminOnly
	@HelpDoc("Locks the bot in channel. Syntax: ?unlockChannel")
	public void unlockChannel() {
		qb.chanLockList.remove(channel);
		qb.sendMessage(channel,"Bot has been unlocked for this channel");
	}
	@AdminOnly
	@HelpDoc("Joins a channel on current server")
	public void joinChan(String channel) {
		qb.joinChannel(channel);
		System.out.println("Joined new channel "+channel);
		qb.sendMessage(channel,sender+": Joined channel "+channel);
	}
	
	@AdminOnly
	@HelpDoc("Parts a channel on current server")
	public void partChan(String channel){
		qb.partChannel(channel);
		System.out.println("Parted channel "+channel);
		qb.sendMessage(channel,sender+": Parted channel "+channel);
	}
}