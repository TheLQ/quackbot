/**
 * @(#)Quackbot.java
 *
 * Quackbot application
 *
 * @author Lord.Quackstar
 * @version 1.00 2010/2/16
 */
 
import org.jibble.pircbot.*;
import java.lang.reflect.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import java.lang.annotation.*;

public class Quackbot extends PircBot {
    
    boolean botLocked = false;
    String channel, sender, login, hostname, rawmsg, command;
    String[] argArray;
    TreeMap<String,Method> methodList;
    TreeMap<String,Method> adminMethodList;
    TreeMap<String,String> adminList;
    TreeMap<String,String> chanLockList;
    final String PREFIX = "?";
    String methodHelp;
    
    //Helpdoc annotation interface
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface HelpDoc {
		String value();
	}
	
	//Admin only annotation interface
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface AdminOnly {}
	
	//Require Arguments annotation interface
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface ReqArg {}
    
    //Init bot by setting all information
    public Quackbot() {
        setName("Quackbot");
        setAutoNickChange(true);
        setFinger("Quackbot IRC bot by Lord.Quackstar. Source: http://github.com/LBlakey/Quackbot");
        setMessageDelay(500);
        setVersion("Quackbot 0.5");
        
        //Get all class information to get known functions
        Class<?> c = this.getClass();
	    Method[] allMethods = c.getDeclaredMethods();
	    //Filter out private methods and constructor
	    methodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);
	    adminMethodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);
	    for(Method method : allMethods) {
	    	int modifier = method.getModifiers();
	    	String name = method.getName();
	    	if(modifier != Modifier.PRIVATE && modifier != Modifier.PROTECTED && !name.equals("onMessage")) {
	    		if(method.getAnnotation(AdminOnly.class) != null) adminMethodList.put(name,method);
	    		else methodList.put(name,method);
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
    		sendMessage(channel, sender+": ERROR: "+e.toString());
    		e.printStackTrace();
    	}
        System.out.println("-----------END BOT ACTIVATED FROM "+message+"-----------");
    }
    
    //Command handling takes place here purley for nice output for console. If returned, end tag still shown
    private void runCommand(String channel, String sender, String login, String hostname, String message) {
    	//Make the class known of thease values
    	this.channel = channel;
    	this.sender = sender;
    	this.login = login;
    	this.hostname = hostname;
    	this.rawmsg = message;
    	
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
    	
    	//Parse message to get cmd and args
    	if(message.indexOf(" ") > -1) {
    		String[] msgArray = message.split(" ",2);
    		this.command = msgArray[0].trim();
    		this.argArray = msgArray[1].split(",");
    	}
    	else {
    		this.command = message.trim();
    		this.argArray = new String[0];
    	}
    	
    	//Does this method exist?
    	if(!methodExists(command)) return;
    	Method reqMethod = methodList.get(command);
    	methodHelp = reqMethod.getAnnotation(HelpDoc.class).value();
    	
    	//Is this an admin function? If so, is the person an admin?
    	if(reqMethod.getAnnotation(AdminOnly.class) != null && !isAdmin()) {
    		sendMessage(channel, sender+": Admin only command");
    		return;
    	}
    	
    	//Does this method require args?
        if(reqMethod.getAnnotation(ReqArg.class) != null) {
        	argArray = new String[]{""};
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
        	reqMethod.invoke(this,(Object[])argArray);
        }
        catch(Exception e) {
        	if(e.getCause() == null) e.printStackTrace();
        	else e.getCause().printStackTrace();
        }
    }
    
    private boolean methodExists(String method) {
    	if(!methodList.containsKey(method)) {
    		sendMessage(channel, sender+": Command "+method+" dosen't exist");
    		return false;
    	}
    	else
    		return true;
    }
    
    private boolean isAdmin() {
    	if(adminList.containsKey(sender)) {
    		System.out.println("Calling user is admin!");
    		return true;
    	}
    	else
    		return false;
    }
    
    /***********************************************************************************
     * BEGIN COMMAND METHODS
     **********************************************************************************/

	/**************************************ADMIN METHODS*******************************/
    @AdminOnly
    @HelpDoc("Locks the bot globaly. Syntax: ?lockBot <true:false>")
    public void lockBot(String Smode) {
    	Boolean mode = Boolean.parseBoolean(Smode);
    	botLocked = mode;
    	sendMessage(channel,"Bot has been "+((mode) ? "locked" : "unlocked")+" globaly");
    }

	@AdminOnly
	@HelpDoc("Locks the bot in channel. Syntax: ?lockChannel <true:false>")
	public void lockChannel(String Smode) {
		Boolean mode = Boolean.parseBoolean(Smode);
		if(mode==true) {
			chanLockList.put(channel,"");
			sendMessage(channel,"Bot has been locked for this channel");
		}
		else
			chanLockList.remove(channel);
	}
	
	/***********************************USER CALLABLE METHODS********************************/
	@ReqArg
    @HelpDoc("Empty: Displays all commands | With command: Displays help for command. Syntax: ?help <OPTIONAL:command>")
    public void help(String method) {
    	if(method.isEmpty()) {
    		//User wants command list
    		StringBuilder cmdList = new StringBuilder();
    		Iterator itr = methodList.keySet().iterator();
    		while(itr.hasNext()) {
    			cmdList.append(itr.next());
    			cmdList.append(", ");
    		}
    		//Remove last space and comma
    		cmdList.deleteCharAt(cmdList.length());
    		cmdList.deleteCharAt(cmdList.length());
    		
    		//Send to user
    		sendMessage(channel, sender + ": Possible commands: "+cmdList.toString());
    	}
    	else {
	    	if(!methodExists(method)) return;
	    	sendMessage(channel, sender + ": "+methodList.get(method).getAnnotation(HelpDoc.class).value());
    	}
    }
    
    @HelpDoc("Returns current time. Syntax: ?time")
    public void time() {
    	String time = new java.util.Date().toString();
        sendMessage(channel, sender + ": The time is now " + time);
    }
}