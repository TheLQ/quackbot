/**
 * @(#)GeneralUser.java
 *
 * General User Commands
 *
 * @author 
 * @version 1.00 2010/2/21
 */

package Quackbot.CMDs;

import Quackbot.*;
import Quackbot.Annotations.*;

import java.lang.reflect.*;
import java.util.*;

public class GeneralUser {
	
	Quackbot qb = null;
	public String channel, sender, login, hostname, rawmsg, command;
	
	public GeneralUser(Quackbot qb) {
		this.qb = qb;
	}
	
	@ReqArg
    @HelpDoc("Empty: Displays all commands | With command: Displays help for command. Syntax: ?help <OPTIONAL:command>")
    public void help(String method) {
    	if(method==null) {
    		//User wants command list
    		StringBuilder cmdList = new StringBuilder();
    		Iterator itr = qb.methodList.entrySet().iterator();
    		while(itr.hasNext()) {
    			Map.Entry<String,Method> currentEntry = (Map.Entry<String,Method>)itr.next();
    			if(!(currentEntry.getValue().getDeclaringClass().getName().equals("Quackbot.CMDs.AdminOnly"))) {
    				cmdList.append(currentEntry.getKey());
    				cmdList.append(", ");
    			}
    		}
    		//Remove last space and comma
    		cmdList.deleteCharAt(cmdList.length()-1);
    		cmdList.deleteCharAt(cmdList.length()-1);
    		
    		//Send to user
    		qb.sendMessage(channel, sender + ": Possible commands: "+cmdList.toString());
    	}
    	else {
	    	if(!qb.methodExists(method)) return;
	    	qb.sendMessage(channel, sender + ": "+qb.methodList.get(method).getAnnotation(HelpDoc.class).value());
    	}
    }
    
    @HelpDoc("Returns current time. Syntax: ?time")
    public void time() {
    	String time = new java.util.Date().toString();
    	String msg = sender + ": The time is now " + time;
        qb.sendMessage(channel, msg);
    }
    
    
}