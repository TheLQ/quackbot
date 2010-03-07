/**
 * @(#)Controller.java
 *
 * Main Controller for bot:
 *  -Holds main thread pool
 *  -Initiates and keeps track of all bots
 *  -Loads (and reload) all CMD classes
 *
 * @author Lord.Quackstar
 */
 
package org.Quackbot;

import java.lang.*;
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
import java.util.concurrent.*;

import org.Quackbot.*;

public class Controller {
	
	public TreeMap<String,TreeMap<String,Object>> cmds;
	public HashSet<Bot> bots = new HashSet<Bot>();
	public ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	public ExecutorService threadPool = Executors.newCachedThreadPool();
	public GUI gui;
	
    public Controller(GUI gui) {
    	//Lets now get all CMD classes and put into array
		cmds = new TreeMap<String,TreeMap<String,Object>>((String.CASE_INSENSITIVE_ORDER));
		
		//Add GUI to instance vars
		this.gui = gui;
		
		//Load current CMD classes
		threadPool.execute(new loadCMDs(this));
		
		//Join some servers
		threadPool.execute(new botThread("irc.freenode.net",new String[]{"##newyearcountdown"}));
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
    
    //Send a message to every channel on every server the bot is connected to
    public void sendGlobalMessage(String msg) {
    	Iterator botItr = bots.iterator();
	   	while(botItr.hasNext()) {
			Bot curBot = (Bot)botItr.next();
			curBot.sendAllMessage(msg);
	   	}
    }
    
    /*****Simple thread to run the bot in to prevent it from locking the gui***/
    public class botThread implements Runnable {
    	String server = null;
    	String[] channels = null;
    	
    	public botThread(String server, String[] channels) {
    		this.server = server;
    		this.channels = channels;
    	}
    	
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