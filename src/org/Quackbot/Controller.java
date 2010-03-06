/**
 * @(#)Controller.java
 *
 * Main Controller for bot:
 *  -Initiates and keeps track of all bots
 *  -Loads (and reload) all CMD classes
 *
 * @author Lord.Quackstar
 */
 
package org.Quackbot;
 
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.net.*;
import javax.script.*;

import org.Quackbot.*;
import org.Quackbot.CMDs.CMDSuper;

public class Controller {
	
	public Map<String,TreeMap<String,Object>> cmds;
	public HashSet<Bot> bots = new HashSet<Bot>();
	public ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	
    public Controller() {
    	//Lets now get all CMD classes and put into array
		cmds = Collections.synchronizedMap(new TreeMap<String,TreeMap<String,Object>>((String.CASE_INSENSITIVE_ORDER)));
		
		//Load current CMD classes
		new loadCMDs(this).start();
		
		//Join some servers
		new botThread("irc.freenode.net",new String[]{"##newyearcountdown"}).start();
    }
    
    //Makes all bots quit servers
    public void stopAll() {
    	Iterator botItr = bots.iterator();
	   	while(botItr.hasNext()) {
			Bot curBot = (Bot)botItr.next();
		    curBot.quitServer("Killed by control panel");
		    bots.remove(curBot);
		}
    }
    
    /*****Simple thread to run the bot in to prevent it from locking the gui***/
    class botThread extends Thread {
    	String server = null;
    	String[] channels = null;
    	
    	public botThread(String server, String[] channels) {
    		this.server = server;
    		this.channels = channels;
    	}
    	      
    	@Override
        public void run() {
        	try {
    			System.out.println("Initiating connection");
    			Bot qb = new Bot(Controller.this);
		        qb.setVerbose(true);
		        qb.connect(server);
		        for(String channel : channels) 
		        	qb.joinChannel(channel);
		        bots.add(qb);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
        }
    }
}