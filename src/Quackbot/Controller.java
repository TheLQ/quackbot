/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 */
 
package Quackbot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Main Controller for bot:
 *  -Holds main thread pool
 *  -Initiates and keeps track of all bots
 *  -Loads (and reload) all CMD classes
 *
 * USED BY: Everything. Initated only by Main
 *
 * @author Lord.Quackstar
 */
public class Controller {
	
	public TreeMap<String,TreeMap<String,Object>> cmds = new TreeMap<String,TreeMap<String,Object>>((String.CASE_INSENSITIVE_ORDER));
	public TreeMap<String,TreeMap<String,Object>> listeners = new TreeMap<String,TreeMap<String,Object>>((String.CASE_INSENSITIVE_ORDER));
	public HashSet<Bot> bots = new HashSet<Bot>();
	public ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	public ExecutorService threadPool = Executors.newCachedThreadPool();
	public ExecutorService threadPool_js = Executors.newCachedThreadPool();
	public Main gui;

	/**
	 * Start of bot working. Loads CMDs and starts Bots
	 * @param gui   Current GUI
	 */
	public Controller(Main gui) {		
		//Add GUI to instance vars
		this.gui = gui;
		
		//Load current CMD classes
		reloadCMDs();
		
		//Join some servers
		threadPool.execute(new botThread("irc.freenode.net",new String[]{"#quackbot"}));
	}

	/**
	 * Makes all bots quit servers
	 */
	public void stopAll() {
		threadPool_js.shutdownNow();
		threadPool_js = null;
		Iterator botItr = bots.iterator();
	   	while(botItr.hasNext()) {
			Bot curBot = (Bot)botItr.next();
			curBot.quitServer("Killed by control panel");
			bots.remove(curBot);
		}
		threadPool.shutdownNow();
	}

	/**
	 * Send a message to every channel on every server the bot is connected to
	 * @param msg   Message to send
	 */
	public void sendGlobalMessage(String msg) {
		Iterator botItr = bots.iterator();
	   	while(botItr.hasNext()) {
			Bot curBot = (Bot)botItr.next();
			curBot.sendAllMessage(msg);
	   	}
	}

	/**
	 * Reload all CMDs
	 */
	public void reloadCMDs() {
	    threadPool.execute(new loadCMDs(this));
	}

	/**
	 * Simple thread to run the bot in to prevent it from locking the gui
	 */
	public class botThread implements Runnable {
		String server = null;
		String[] channels = null;

		/**
		 * Define some simple variables
		 * @param server
		 * @param channels
		 */
		public botThread(String server, String[] channels) {
			this.server = server;
			this.channels = channels;
		}

		/**
		 * Initiates bot, joins it to some channels
		 */
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
