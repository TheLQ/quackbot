/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.Channel;
import Quackbot.info.Server;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import jpersist.DatabaseManager;

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
    DatabaseManager dbm = null;
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

	//Connect to database
	dbm = new DatabaseManager("quackbot", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/quackbot", null, null, "root", null);

	//Get all servers and pass to botThread generator
	try {
	    Collection<Server> c = dbm.loadObjects(new ArrayList<Server>(), Server.class);
	    for (Server curServer : c) {
		dbm.loadAssociations(curServer);
		threadPool.execute(new botThread(curServer));
	    }
	} catch (Exception e) {
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

    public void addServer(String address, String... channels) {
	//TODO
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

	Server server = null;

	/**
	 * Define some simple variables
	 * @param server
	 * @param channels
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
		Bot qb = new Bot(Controller.this,server);
		qb.setVerbose(true);
		bots.add(qb);
	    } catch (Exception ex) {
		log.error("Can't make bot connect to server", ex);
	    }
	}
    }
}
