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

import org.Quackbot.*;
import org.Quackbot.CMDs.CMDSuper;

import org.apache.commons.jci.*;
import org.apache.commons.jci.compilers.*;
import org.apache.commons.jci.readers.*;
import org.apache.commons.jci.stores.*;

public class Controller {
	
	public TreeMap<String,CMDSuper> cmds;
	public TreeMap<String,Method> methodList;
	public HashSet<Bot> bots = new HashSet<Bot>();
	public HashSet<URLClassLoader> classLoaders = new HashSet<URLClassLoader>();
	
    public Controller() {
    	//Lets now get all CMD classes and put into array
		cmds = new TreeMap<String,CMDSuper>();
		methodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);

		//Load current CMD classes
		loadCMDs loader = new loadCMDs(this);
		loader.execute();
		
		//Join some servers
		new botThread("irc.freenode.net",new String[]{"##newyearcountdown"}).execute();
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
    class botThread extends SwingWorker<Void, String> {
    	String server = null;
    	String[] channels = null;
    	
    	public botThread(String server, String[] channels) {
    		this.server = server;
    		this.channels = channels;
    	}
    	      
    	@Override
        public Void doInBackground() {
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
			return null;
        }
    }
}