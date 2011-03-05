/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot;

import Quackbot.gui.GUI;
import Quackbot.hook.HookManager;
import Quackbot.info.Admin;
import Quackbot.info.Channel;
import Quackbot.info.Server;
import ch.qos.logback.classic.Level;
import ejp.DatabaseException;
import ejp.DatabaseManager;
import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quackbot is an advanced IRC bot framework based off of PircBot.
 * <p>
 * At the core, its a ready made IRC bot that does everything a normal bot should:
 * Support for connection to multiple servers, Proper multithreading, abstraction
 * of commands, hooks into any part of execution, and persistence by database
 * <p>
 * This is the main class of Quackbot, configuring and managing everything. There should
 * only be ONE instance, which is accessable with {@link #instance}.
 * <p>
 * If this is being used in a larger project, it is highly recommended to start this in its own thread
 * <p>
 * A minimal setup should look like this:
 * <br>
 * <code>
 *      Controller ctrl = new Controller(); //Initalize
 *		ctrl.connectDB("databasename", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://url.com/databasename", null, null, "username", "password"); //connect to database
 *		ctrl.start(); //Execute Quackbot
 * </code>
 * <br>
 * All of those calls are absolutely nessesary. Leaving out connectDB will yield a QuackbotException, leaving out start will just not do anything
 * <p>
 * Other methods of intrest include {@link #addCommand(Quackbot.PluginLoader)}, {@link #addPrefix(java.lang.String) },
 * {@link #addServer(java.lang.String, int, java.lang.String[]) }, and {@link #initHooks}
 * <p>
 * Please read documentation for more explination
 * 
 * @author Lord.Quackstar
 */
public class Controller {
	/**
	 * Set of all Bot instances
	 */
	public TreeMap<String, Bot> bots = new TreeMap<String, Bot>();
	/**
	 * Number of Commands executed, used by logging
	 */
	protected int cmdNum = 0;
	/**
	 * Log4j Logger
	 */
	private Logger log = LoggerFactory.getLogger(Controller.class);
	/**
	 * ThreadPool that all non-bot threads are executed in
	 */
	public static final ExecutorService mainPool = Executors.newCachedThreadPool(/*new ThreadFactory() {
			public int count = 0;
			public ThreadGroup threadGroup = new ThreadGroup("mainPool");

			@Override
			public Thread newThread(Runnable r) {
			System.out.println("New thread for runnable "+r.toString());
			return new Thread(threadGroup, "mainPool-" + (++count));
			}
			}*/);
	/**
	 * The full configuration avaliable to us
	 */
	public QuackbotConfig config;
	/**
	 * The Logger
	 */
	protected ControlAppender appender;
	public GUI gui;

	/**
	 * Init for Quackbot. Sets instance, adds shutdown hook, and starts GUI if requested
	 * @param makeGui  Show the GUI or not. WARNING: If there is no GUI, a slf4j Logging
	 *                 implementation <b>must</b> be provided to get any outpu
	 */
	public Controller(QuackbotConfig config) {
		this.config = config;

		//Setup logger
		ch.qos.logback.classic.Logger rootLog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
		rootLog.getLoggerContext().reset();
		rootLog.setLevel(Level.ALL);
		rootLog.detachAndStopAllAppenders();
		rootLog.addAppender(appender = new ControlAppender(this,rootLog.getLoggerContext()));

		//Add shutdown hook to kill all bots and connections
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Logger log = LoggerFactory.getLogger(this.getClass());
				log.info("Closing all IRC and db connections gracefully");

				Controller.this.stopAll();
				try {
					if (Controller.this.getDatabase() != null)
						Controller.this.getDatabase().close();
				} catch (Exception e) {
					e.printStackTrace(); //send to standard output because window is closing
				}
			}
		});

		//Do we need to make a GUI?
		if (config.isGuiEnabled())
			try {
				//This can't run in EDT, end if it is
				if (SwingUtilities.isEventDispatchThread()) {
					log.error("Controller cannot be started from EDT. Please start from seperate thread");
					return;
				}

				//Attempt to dynamically load GUI since it might not exist in packages
				gui = new GUI(this);
			} catch (Exception e) {
				log.error("Unkown error occured in GUI initialzation", e);
			}

		//Blindly load plugin defaults
		try {
			Class.forName("Quackbot.plugins.JavaPluginLoader");
		} catch (Exception e) {
			log.trace("Loading JavaPluginLoader for setup has silently failed", e);
		}

	}

	/**
	 * Executues Quackbot. Loads commands, starts service commands, connects to servers.
	 * If this isn't called, then the bot does nothing
	 */
	public void start() {
		if (config.getDatabase() == null) {
			log.error("Not configured to use database! Must run connectDB ");
			return;
		}

		//Call list of commands
		HookManager.getHookMap("onInit").execute(this);

		//Load current CMD classes
		reloadPlugins();

		if(true)
		return;

		//Connect to all servers
		try {
			Collection<Server> c = getDatabase().loadObjects(new ArrayList<Server>(), Server.class);
			if (c.isEmpty())
				log.error("Server list is empty!");
			for (Server curServer : c)
				initBot(curServer);
		} catch (Exception e) {
			if (e instanceof DatabaseException)
				if (StringUtils.contains(e.getMessage(), "Communications link failure"))
					log.error("Error in connecting to database. Please check database connectivity and restart application", e);
				else
					log.error("Database error", e);
			else
				log.error("Error encountered while attempting to join servers", e);
		}
	}

	/**
	 * Starts bot using server object
	 * @param curServer
	 */
	public void initBot(final Server curServer) {
		final ExecutorService threadPool = newBotPool(curServer.getAddress());
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Initiating IRC connection to server " + curServer);
					Bot qb = new Bot(Controller.this, curServer, threadPool);
					qb.setVerbose(true);
					bots.put(curServer.getAddress(), qb);
				} catch (Exception ex) {
					log.error("Can't make bot connect to server", ex);
				}
			}
		});
	}

	/**
	 * Makes all bots quit servers
	 */
	public void stopAll() {
		for (Bot curBot : bots.values()) {
			curBot.quitServer("Killed by control panel");
			curBot.dispose();
		}
		bots.clear();
		log.info("Killed all bots");
	}

	/**
	 * Creates a new server, adds to database, and connects
	 * @param address  Address of server
	 * @param port     Port number to be used (if null, the 6667 is used)
	 * @param channels Vararg of channels to join
	 */
	public void addServer(String address, int port, String... channels) {
		Server srv = new Server(address, port);
		for (String curChan : channels)
			srv.addChannel(new Channel(curChan));
		initBot(srv.updateDB(this));
	}

	/**
	 * Deletes a server by address name, removing from database. This will NOT disconnect
	 * the associated bot.
	 * @param address  The address of the server to be deleted
	 */
	public void removeServer(String address) {
		try {
			Collection<Server> c = getDatabase().loadObjects(new ArrayList<Server>(), Server.class);
			for (Server curServ : c)
				if (curServ.getAddress().equals(address))
					curServ.delete(this);
		} catch (Exception e) {
			log.error("Can't remove server", e);
		}
	}

	/**
	 * Send a message to every channel on every server Quackbot is connected to. Use carefully!
	 * @param msg   Message to send
	 */
	public void sendGlobalMessage(String msg) {
		for (Bot curBot : bots.values())
			curBot.sendAllMessage(msg);
	}

	/**
	 * Reloads all commands, clearing list only if requested
	 * @param clean Clear list of commands?
	 */
	public void reloadPlugins() {
		HookManager.getHookMap("onPluginLoadStart").execute();
		CommandManager.removeAll();

		try {
			//Load all permanent commands
			reloadPlugins(new File("plugins"));
			HookManager.getHookMap("onPluginLoadComplete").execute();
		} catch (Exception e) {
			log.error("Error in plugin loading!!!", e);
		}
	}

	/**
	 * Recusrivly load commands from current file. Use
	 * @param file
	 */
	protected void reloadPlugins(File file) {
		String[] extArr = null;
		//Load using appropiate type
		try {
			if (file.isDirectory()) {
				final File[] childs = file.listFiles();
				for (File child : childs)
					reloadPlugins(child);
				return;
			} //Is this in the .svn directory?
			else if (file.getAbsolutePath().indexOf(".svn") != -1)
				return;

			//Get extension of file
			extArr = StringUtils.split(file.getName(), '.');
			if (extArr.length < 2)
				return;
			String ext = extArr[1];

			//Load with pluginType
			PluginLoader loader = config.getPluginLoaders().get(ext);
			if (loader != null)
				loader.load(file);
		} catch (Exception e) {
			log.error("Could not load plugin " + extArr[0], e);
		}
	}

	/**
	 * Increments command number and returns new int
	 */
	public synchronized int addCmdNum() {
		return ++cmdNum;
	}

	/**
	 * Generates a ThreadPool for {@link Bot}
	 * @param address
	 * @return Fully configured ThreadPool
	 */
	public ExecutorService newBotPool(final String address) {
		return Executors.newCachedThreadPool(new ThreadFactory() {
			int threadCounter = 0;
			List<String> usedNames = new ArrayList<String>();
			ThreadGroup threadGroup;

			@Override
			public Thread newThread(Runnable rbl) {
				String goodAddress = address;

				int counter = 0;
				while (usedNames.contains(goodAddress))
					goodAddress = address + "-" + (counter++);

				if (threadGroup == null)
					threadGroup = new ThreadGroup("quackbot-" + goodAddress);
				return new Thread(threadGroup, rbl, "quackbot-" + goodAddress + "-" + threadCounter++);
			}
		});
	}

	public boolean isAdmin(String name, Bot bot, String channel) {
		return isAdmin(name, bot.getServer(), channel);
	}

	/**
	 * Utility to check if an admin exists in either the global scope,
	 * server scope, or channel scope
	 * @param bot
	 * @return True if admin exists, false otherwise
	 */
	public boolean isAdmin(String name, String server, String channel) {
		try {
			Collection<Admin> c = getDatabase().loadObjects(new ArrayList<Admin>(), Admin.class);
			for (Admin curAdmin : c) {
				//Is this even a match?
				if (!curAdmin.getUser().equalsIgnoreCase(name))
					continue;

				//Is this person an admin of this channel?
				Channel chan = curAdmin.getChannel(this);
				if (chan != null && chan.getName().equals(channel))
					return true;

				//Is this person an admin of the server?
				Server serv = curAdmin.getServer(this);
				if (serv != null && serv.getAddress().equalsIgnoreCase(server))
					return true;

				//Is this person a global admin?
				if (serv != null && chan != null)
					return true;
			}
		} catch (DatabaseException e) {
			log.error("Couldn't finish finding admin", e);
		}
		return false;
	}

	public DatabaseManager getDatabase() {
		return config.getDatabase();
	}
}