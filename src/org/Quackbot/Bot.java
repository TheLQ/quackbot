/**
 * @(#)Bot.java
 *
 * Bot instance that communicates with 1 server
 *  -Initiates all commands
 *
 * @author Lord.Quackstar
 */
 
package org.Quackbot;
 
import org.jibble.pircbot.*;
import java.lang.reflect.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import java.lang.annotation.*;

import org.Quackbot.Annotations.*;
import org.Quackbot.CMDs.*;
import org.Quackbot.*;

public class Bot extends PircBot {
    
    public boolean botLocked = false;

    final String PREFIX = "?";
    
    String channel, sender;
    
	public TreeMap<String,String> adminList;
    public TreeMap<String,String> chanLockList;
    public Controller mainInst = null;
    
    //Init bot by setting all information
    public Bot(Controller mainInstance) {
    	mainInst = mainInstance;
        setName("Quackbot");
        setAutoNickChange(true);
        setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://github.com/LBlakey/Quackbot");
        setMessageDelay(500);
        setVersion("Quackbot 0.5");
    }
    
    //Setup bot when fully connected
    @Override
    public void onConnect() {
	    //Add admins
	    adminList = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	    adminList.put("LordQuackstar","True");
	    
	    //Init channel block list
	    chanLockList = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
    }
    
	//Activated when someone types a message
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		//Make class aware of a few parameters
		this.channel = channel;
		this.sender = sender;
		
    	//Is there a prefix?
    	if(!message.substring(0,PREFIX.length()).equals(PREFIX))
    		return;
    	message = message.substring(PREFIX.length(),message.length()).toLowerCase();    	
    	
    	//Bot activated, start command process
    	System.out.println("-----------BOT ACTIVATED FROM "+message+"-----------");
    	try {
    		runCommand(channel, sender, login, hostname, message);
    	}
    	catch(Exception e) {
    		sendMessage(channel, sender+": RUN ERROR: "+e.toString());
    		e.printStackTrace();
    	}
        System.out.println("-----------END BOT ACTIVATED FROM "+message+"-----------");
    }
    
    //Command handling takes place here purley for nice output for console. If returned, end tag still shown
    private void runCommand(String channel, String sender, String login, String hostname, String rawmsg) {
    	
    	//Is bot locked?
    	if(botLocked == true && !isAdmin()) {
    		System.out.println("Command ignored due to global lock in effect");
    		return;
    	}
    	
    	//Is channel locked?
    	if(chanLockList.containsKey(channel)) {
 			System.out.println("Command ignored due to channel lock in effect");
    		return;
    	}
    	
    	String[] argArray;
    	String command;
    	//Parse message to get cmd and args
    	if(rawmsg.indexOf(" ") > -1) {
    		String[] msgArray = rawmsg.split(" ",2);
    		command = msgArray[0].trim();
    		argArray = msgArray[1].split(",");
    	}
    	else {
    		command = rawmsg.trim();
    		argArray = new String[0];
    	}
    	
    	//Does this method exist?
    	if(!methodExists(command)) return;
    	Method reqMethod = mainInst.methodList.get(command);
    	
    	//Is this an admin function? If so, is the person an admin?
    	if(reqMethod.getDeclaringClass().getName().equals("AdminOnly") && !isAdmin()) {
    		sendMessage(channel, sender+": Admin only command");
    		return;
    	}
    	
    	//Does this method require args?
        if(reqMethod.getAnnotation(ReqArg.class) != null && argArray.length == 0) {
        	System.out.println("Method does require args, passing length 1 array");
        	argArray = new String[1];
        }
    	
    	//Does the required number of args exist?
    	Class[] parameterTypes = reqMethod.getParameterTypes();
        int user_args = argArray.length;
        int method_args = parameterTypes.length;
        System.out.println("User Args: "+user_args+" | Req Args: "+method_args);
        if(user_args != method_args) {
        	sendMessage(channel, sender+": Wrong number of parameters specified. Given: "+user_args+", Required: "+method_args);
    		return;
        }
        
        //All requirements are met, excecute method
        System.out.println("All tests passed, running method");
        try {
        	String reqClassName = reqMethod.getDeclaringClass().getName().split("\\.")[3];
        	CMDSuper reqClass = mainInst.cmds.get(reqClassName);
        	System.out.println("Trying to get class: "+reqClassName);
        	reqClass.update(channel,sender,login,hostname,rawmsg,command,this);
	        reqMethod.invoke(reqClass,(Object[])argArray);
        }
        catch(Exception e) {
        	Throwable cause = e.getCause();
        	//cause.printStackTrace();
        	e.printStackTrace();
        	sendMessage(channel, sender+": CMD ERROR: "+e.toString());
        }
    }
    
    public boolean methodExists(String method) {
    	if(!mainInst.methodList.containsKey(method)) {
    		sendMessage(channel, sender+": Command "+method+" dosen't exist");
    		return false;
    	}
    	else
    		return true;
    }
    
    public boolean isAdmin() {
    	if(adminList.containsKey(sender)) {
    		System.out.println("Calling user is admin!");
    		return true;
    	}
    	else
    		return false;
    }
}