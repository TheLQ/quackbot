/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.Channel;
import Quackbot.info.Server;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;

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

	public TreeMap<String, TreeMap<String, Object>> cmds = new TreeMap<String, TreeMap<String, Object>>((String.CASE_INSENSITIVE_ORDER));
	public TreeMap<String, TreeMap<String, Object>> listeners = new TreeMap<String, TreeMap<String, Object>>((String.CASE_INSENSITIVE_ORDER));
	public TreeMap<String, TreeMap<String, Object>> services = new TreeMap<String, TreeMap<String, Object>>((String.CASE_INSENSITIVE_ORDER));
	public TreeSet<String> utils = new TreeSet<String>();
	public HashSet<Bot> bots = new HashSet<Bot>();
	public ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	public ExecutorService threadPool = Executors.newCachedThreadPool();
	public ExecutorService threadPool_js = Executors.newCachedThreadPool();
	public Main gui;
	Logger log = Logger.getLogger(Controller.class);

	/**
	 * Start of bot working. Loads CMDs and starts Bots
	 * @param gui   Current GUI
	 */
	public Controller(Main gui) {
		//Add GUI to instance vars
		this.gui = gui;

		//Load current CMD classes
		reloadCMDs();

		//Connect to JackRabbit DB and join servers
		try {
		    threadPool.execute(new botThread("irc.freenode.net", Arrays.asList(new String[]{"#quackbot"})));
		} catch (Exception e) {
		    e.printStackTrace();
			//log.error("Error in JackRabbit or IRC connection", e);
		    log.error(e,e);
		}
	}

	/**
	 * Makes all bots quit servers
	 */
	public void stopAll() {
		Iterator botItr = bots.iterator();
		while (botItr.hasNext()) {
			Bot curBot = (Bot) botItr.next();
			curBot.quitServer("Killed by control panel");
			curBot.dispose();
			bots.remove(curBot);
		}
		threadPool_js.shutdownNow();
		threadPool_js = null;
		threadPool.shutdownNow();
		threadPool = null;
		log.info("Killed all bots, threadPools, and JackRabbit connection");
	}

	public void addServer(String address, String... channels) {
		Server newServ = new Server();
		for (String chan : channels) {
			newServ.addChannel(new Channel(chan));
		}
		//TODO: Add to database
		threadPool.execute(new botThread(newServ.getAddress(), null));
		log.info("Added server " + newServ.getAddress());
	}

	public void removeServer(String address) {
		try {
		    //TODO
		} catch (Exception e) {
			log.error("Can't remove server", e);
		}
	}

	/**
	 * Send a message to every channel on every server the bot is connected to
	 * @param msg   Message to send
	 */
	public void sendGlobalMessage(String msg) {
		Iterator botItr = bots.iterator();
		while (botItr.hasNext()) {
			Bot curBot = (Bot) botItr.next();
			curBot.sendAllMessage(msg);
		}
	}

	/**
	 * Reload all CMDs
	 */
	public void reloadCMDs() {
		threadPool_js.shutdownNow();
		threadPool_js = Executors.newCachedThreadPool();
		threadPool.shutdownNow();
		threadPool = Executors.newCachedThreadPool();
		threadPool.execute(new loadCMDs(this));
	}

	/**
	 * Simple thread to run the bot in to prevent it from locking the gui
	 */
	public class botThread implements Runnable {

		String server = null;
		List<String> channels = null;

		/**
		 * Define some simple variables
		 * @param server
		 * @param channels
		 */
		public botThread(String server, List<String> channels) {
			this.server = server;
			this.channels = channels;
		}

		/**
		 * Initiates bot, joins it to some channels
		 */
		public void run() {
			try {
				log.info("Initiating IRC connection");
				Bot qb = new Bot(Controller.this, server, 6665);
				qb.setVerbose(true);
				Iterator chanItr = channels.iterator();
				while (chanItr.hasNext()) {
					Channel curChan = (Channel) chanItr.next();
					String channel = curChan.getName();
					qb.joinChannel(channel);
					log.debug("Channel: " + channel);
				}
				bots.add(qb);
			} catch (Exception ex) {
				log.error("Can't make bot connect to server", ex);
			}
		}
	}
}
