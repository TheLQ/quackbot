/**
 * @(#)Quackbot.java
 *
 * Quackbot application
 *
 * @author Lord.Quackstar
 * @version 1.00 2010/2/16
 */
 
package Quackbot;
 
import org.jibble.pircbot.*;
import java.lang.reflect.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import java.lang.annotation.*;

import Quackbot.Annotations.*;
import Quackbot.CMDs.*;

public class Quackbot extends PircBot {
    
    public boolean botLocked = false;

    final String PREFIX = "?";
    
    String channel, sender;
    
    public TreeMap<String,Method> methodList;
    public TreeMap<String,Method> adminMethodList;
    public TreeMap<String,String> adminList;
    public TreeMap<String,String> chanLockList;
    
    //Init bot by setting all information
    public Quackbot() {
        setName("Quackbot");
        setAutoNickChange(true);
        setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://github.com/LBlakey/Quackbot");
        setMessageDelay(500);
        setVersion("Quackbot 0.5");
    }
    
    //Setup bot when fully connected
    @Override
    public void onConnect() {
    	//Get all methods from all of the child classes into one array
	    Collection<Method> col1 = Arrays.asList(QBMain.AdminOnly.getClass().getDeclaredMethods());
	    HashSet<Method> sorter = new HashSet<Method>(col1);
	    sorter.addAll(Arrays.asList(QBMain.GeneralUser.getClass().getDeclaredMethods()));
	    Method[] allMethods = sorter.toArray(new Method[sorter.size()]);

	    //Start adding to class list
	    methodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);
	    adminMethodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);
	    for(Method method : allMethods) {
	    	int modifier = method.getModifiers();
	    	String name = method.getName();
	    	if(modifier != Modifier.PRIVATE && modifier != Modifier.PROTECTED && !name.equals("onMessage")) {
	    		methodList.put(name,method);
	    		System.out.println("Name: "+name);
	    	}
	    }
	    
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
    	Method reqMethod = methodList.get(command);
    	
    	//Is this an admin function? If so, is the person an admin?
    	if(reqMethod.getDeclaringClass().getName().equals("AdminOnly") && !isAdmin()) {
    		sendMessage(channel, sender+": Admin only command");
    		return;
    	}
    	
    	//Does this method require args?
        if(reqMethod.getAnnotation(ReqArg.class) != null) {
        	System.out.println("Method does require args, passing length 1 array");
        	argArray = new String[1];
        }
    	
    	//Is the required number of args exist?
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
        	String reqClassName = reqMethod.getDeclaringClass().getName();
        	if(reqClassName.equals("Quackbot.CMDs.AdminOnly")) {
        		AdminOnly reqClass = QBMain.AdminOnly;
        		reqClass.channel = channel;
	        	reqClass.sender = sender;
	        	reqClass.login = login;
	        	reqClass.hostname = hostname;
	        	reqClass.rawmsg = rawmsg;
	        	reqMethod.invoke(reqClass,(Object[])argArray);
        	}
        	else if(reqClassName.equals("Quackbot.CMDs.GeneralUser")) {
        		GeneralUser reqClass = QBMain.GeneralUser;
        		reqClass.channel = channel;
	        	reqClass.sender = sender;
	        	reqClass.login = login;
	        	reqClass.hostname = hostname;
	        	reqClass.rawmsg = rawmsg;
	        	reqMethod.invoke(reqClass,(Object[])argArray);
        	}
        	else
        		System.out.println("Cannot find class "+reqClassName);
        }
        catch(Exception e) {
        	Throwable cause = e.getCause();
        	cause.printStackTrace();
        	sendMessage(channel, sender+": CMD ERROR: "+cause.toString());
        }
    }
    
    public boolean methodExists(String method) {
    	if(!methodList.containsKey(method)) {
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