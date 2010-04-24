/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.Channel;
import Quackbot.info.JSPlugin;
import Quackbot.info.JavaPlugin;
import Quackbot.info.Server;
import Quackbot.plugins.core.Help;

import Quackbot.plugins.core.JavaTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


import jpersist.DatabaseManager;
import jpersist.JPersistException;
import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

/**
 * Main Controller for bot:
 *  -Holds main thread pool
 *  -Initiates and keeps track of all bots
 *  -Loads (and reload) all CMD classes
 *
 * USED BY: Everything. Initated only by Main
 *
 * There should only be <b>1</b> instance of this. It can be refrenced by {@link Quackbot.InstanceTracker#getController() InstanceTracker.getController}
 *
 * @author Lord.Quackstar
 */
public class Controller {

	/**
	 * TreeMap of all JS plugins
	 */
	public TreeMap<String, JSPlugin> JSplugins = new TreeMap<String, JSPlugin>();
	/**
	 * TreeSet of all JS utils
	 */
	public TreeSet<String> JSUtils = new TreeSet<String>();
	/**
	 * List of Fully Qualified Class names of all Java Plugins
	 */
	public final List<JavaPlugin> javaPlugins = Arrays.asList(
		new JavaPlugin(JavaTest.class.getName()),
		new JavaPlugin(Help.class.getName()));
	/**
	 * Set of all Bot instances
	 */
	public HashSet<Bot> bots = new HashSet<Bot>();
	
	/**
	 * Current {@link Main} instance
	 */
	public Main gui = InstanceTracker.getMain();
	/**
	 * DatabaseManager instance of JPersist database
	 */
	public DatabaseManager dbm = null;
	/**
	 * Log4j Logger
	 */
	private Logger log = Logger.getLogger(Controller.class);

	/**
	 * Start of bot working.
	 * -Loads CMDs
	 * -Connects to database
	 * -starts Bots from database info
	 */
	public Controller() {
		InstanceTracker.setController(this);

		//Load current CMD classes
		reloadPlugins();

		//Connect to database
		DatabaseManager.setLogLevel(java.util.logging.Level.OFF);
		dbm = new DatabaseManager("quackbot", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://192.168.2.11/quackbot", null, null, "root", null);

		//Get all server objects from database
		Collection<Server> c = null;
		try {
			c = dbm.loadObjects(new ArrayList<Server>(), Server.class,true);
			if(c.size() == 0)
				log.fatal("Server list is empty!");
			for (Server curServer : c) {
				dbm.loadAssociations(c);
				ThreadPoolManager.addMain(new botThread(curServer));
			}
		} catch (Exception e) {
			if (e instanceof JPersistException) {
				if (StringUtils.contains(e.getMessage(), "Communications link failure"))
					log.fatal("Error in connecting to database. Please check database connectivity and restart application", e);
				else
					log.fatal("Database error", e);
			}
			else
				log.fatal("Error encountered while attempting to join servers", e);
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
		log.info("Killed all bots");
	}

	/**
	 * Creates a new server, adds to database, and joins
	 * @param address  Address of server
	 * @param port     Port number to be used (if null, the 6667 is used)
	 * @param channels Vararg of channels to join
	 */
	public void addServer(String address, int port, String... channels) {
		Server srv = new Server(address,6667);
		for(String curChan : channels)
			srv.addChannel(new Channel(curChan));
		srv.updateDB();
	}

	/**
	 * Deletes a server by address name, removing from database. Will disconnect if nessesary
	 * @param address  The address of the server to be deleted
	 */
	public void removeServer(String address) {
		try {
			Collection<Server> c = dbm.loadObjects(new ArrayList<Server>(), Server.class);
			for(Server curServ : c) {
				if(curServ.getAddress().equals(address))
					curServ.delete();
			}
		} catch (Exception e) {
			log.error("Can't remove server", e);
		}
	}

	/**
	 * Send a message to every channel on every server the bot is connected to
	 * @param msg   Message to send
	 */
	public void sendGlobalMessage(String msg) {
		for (Bot curBot : bots)
			curBot.sendAllMessage(msg);
	}

	/**
	 * Reload all plugins
	 *
	 * Note: This does shutdown all the thread pools (Bot instances are unaffected).
	 *	Take this into account if you have services running in the background
	 */
	public void reloadPlugins() {
		ThreadPoolManager.addMain(new loadCMDs());
	}

	/**
	 * Simple thread to run the bot in to prevent it from locking the gui
	 */
	public class botThread implements Runnable {

		Server server = null;

		/**
		 * Define some simple variables
		 * @param server
		 */
		public botThread(Server server) {
			this.server = server;
		}

		/**
		 * Initiates bot, joins it to some channels
		 */
		public void run() {
			try {
				log.info("Initiating IRC connection");
				Bot qb = new Bot(server);
				qb.setVerbose(true);
				bots.add(qb);
			} catch (Exception ex) {
				log.error("Can't make bot connect to server", ex);
			}
		}
	}
}
