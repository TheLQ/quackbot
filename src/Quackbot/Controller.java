/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.JSPlugin;
import Quackbot.info.JavaPlugin;
import Quackbot.info.Server;
import Quackbot.plugins.core.Help;
import Quackbot.plugins.core.JavaHelp;

import Quackbot.plugins.core.JavaTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import jpersist.DatabaseManager;
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
 * There should only be <b>1</b> instance of this. It can be refrenced by {@link Quackbot.InstanceTracker#getCtrlInst() InstanceTracker.getCtrlInst}
 *
 * @author Lord.Quackstar
 */
public class Controller {

	/**
	 * TreeMap of all JS plugins
	 */
	public TreeMap<String, JSPlugin> JSplugins = new TreeMap<String, JSPlugin>();
	/**
	 * List of Fully Qualified Class names of all Java Plugins
	 */
	public final List<JavaPlugin> javaPlugins = Arrays.asList(
		new JavaPlugin(JavaTest.class.getName()),
		new JavaPlugin(JavaHelp.class.getName()),
		new JavaPlugin(Help.class.getName()));
	/**
	 * Set of all Bot instances
	 */
	public HashSet<Bot> bots = new HashSet<Bot>();
	/**
	 * Thread pool for all non <i>bot generated</i> commands
	 */
	public ExecutorService threadPool = Executors.newCachedThreadPool();
	/**
	 * Thread pool for all <i>bot generated</i> commands
	 */
	public ExecutorService threadPool_js = Executors.newCachedThreadPool();
	/**
	 * Current {@link Main} instance
	 */
	public Main gui = InstanceTracker.getMainInst();
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
		InstanceTracker.setCtrlInst(this);

		//Load current CMD classes
		reloadPlugins();

		//Connect to database
		DatabaseManager.setLogLevel(java.util.logging.Level.OFF);
		dbm = new DatabaseManager("quackbot", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/quackbot", null, null, "root", null);

		//Get all server objects from database
		Collection<Server> c = null;
		try {
			c = dbm.loadObjects(new ArrayList<Server>(), Server.class, true);
			for (Server curServer : c)
				threadPool.execute(new botThread(curServer));

		} catch (Exception e) {
			if (StringUtils.contains(e.getMessage(), "Communications link failure"))
				log.fatal("Error in connecting to database. Please check database connectivity and restart application", e);
			else
				log.error("Could not connect to server", e);
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

	/**
	 * Creates a new server, adds to database, and joins
	 * @param address  Address of server
	 * @param port     Port number to be used (if null, the 6667 is used)
	 * @param channels Vararg of channels to join
	 */
	public void addServer(String address, int port, String... channels) {
		//TODO
	}

	/**
	 * Deletes a server by address name, removing from database. Will disconnect if nessesary
	 * @param address  The address of the server to be deleted
	 */
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
		threadPool_js.shutdownNow();
		threadPool_js = Executors.newCachedThreadPool();
		threadPool.shutdownNow();
		threadPool = Executors.newCachedThreadPool();
		threadPool.execute(new loadCMDs());
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
