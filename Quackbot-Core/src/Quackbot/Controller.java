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

import Quackbot.hook.HookManager;
import Quackbot.info.Admin;
import Quackbot.info.Channel;
import Quackbot.info.Server;
import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.sql.DataSource;
import javax.swing.SwingUtilities;

import jpersist.DatabaseManager;
import jpersist.JPersistException;
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
 * All of those calls are absolutly nessesary. Leaving out connectDB will yeild a QuackbotException, leaving out start will just not do anything
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
	 * Singleton instance.
	 */
	public static Controller instance;
	/**
	 * Global Prefixes.
	 */
	public List<String> globPrefixes = Collections.synchronizedList(new ArrayList<String>());
	/**
	 * All registered plugin types
	 */
	public static TreeMap<String, PluginLoader> pluginLoaders = new TreeMap<String, PluginLoader>();
	/**
	 * Set of all Bot instances
	 */
	public TreeMap<String, Bot> bots = new TreeMap<String, Bot>();
	/**
	 * DatabaseManager instance of JPersist database
	 */
	public DatabaseManager dbm = null;
	/**
	 * Number of Commands executed, used by logging
	 */
	public int cmdNum = 0;
	/**
	 * Log4j Logger
	 */
	private Logger log = LoggerFactory.getLogger(Controller.class);
	/**
	 * Has JPersist had its level set?
	 */
	private java.util.logging.Level setLevel = java.util.logging.Level.OFF;
	/**
	 * ThreadPool that all non-bot threads are executed in
	 */
	public static final ExecutorService mainPool = Executors.newCachedThreadPool(new ThreadFactory() {
		public int count = 0;
		public ThreadGroup threadGroup = new ThreadGroup("mainPool");

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(threadGroup, "mainPool-" + (++count));
		}
	});
	/**
	 * Wait between sending messages
	 */
	public static int msgWait = 1750;

	/**
	 * Convience method for <code>new Controller(true)</code>
	 */
	public Controller() {
		this(true);
	}

	/**
	 * Init for Quackbot. Sets instance, adds shutdown hook, and starts GUI if requested
	 * @param makeGui  Show the GUI or not. WARNING: If there is no GUI, a slf4j Logging
	 *                 implementation <b>must</b> be provided to get any outpu
	 */
	public Controller(boolean makeGui) {
		instance = this;

		//Add shutdown hook to kill all bots and connections
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Logger log = LoggerFactory.getLogger(this.getClass());
				log.info("Closing all IRC and db connections gracefully");
				Controller ctrl = Controller.instance;
				ctrl.stopAll();
				try {
					if (ctrl.dbm != null)
						ctrl.dbm.close();
				} catch (Exception e) {
					e.printStackTrace(); //send to standard output because window is closing
				}
			}
		});

		//Do we need to make a GUI?
		if (makeGui) {
			//This can't run in EDT, end if it is
			if (SwingUtilities.isEventDispatchThread()) {
				String errormsg = "Controller cannot be started from EDT. Please start from seperate thread";
				log.error(errormsg);
				System.err.println(errormsg);
				return;
			}

			//Attempt to dynamically load GUI since it might not exist in packages
			try {
				getClass().getClassLoader().loadClass("Quackbot.GUI").newInstance();
			} catch (Exception e) {
				log.error("Unable to start GUI", e);
			}
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
		if (dbm == null) {
			log.error("Not configured to use database! Must run connectDB ");
			return;
		}

		//Logging level of JPersist
		DatabaseManager.setLogLevel(setLevel);

		//Call list of commands
		HookManager.getList("onInit").execute();

		//Load current CMD classes
		reloadPlugins();

		//if(true)
		//return;

		//Connect to all servers
		try {
			Collection<Server> c = dbm.loadObjects(new ArrayList<Server>(), Server.class, true);
			if (c.isEmpty())
				log.error("Server list is empty!");
			for (Server curServer : c)
				initBot(curServer);
		} catch (Exception e) {
			if (e instanceof JPersistException)
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
					Bot qb = new Bot(curServer, threadPool);
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
		initBot(srv.updateDB());
	}

	/**
	 * Deletes a server by address name, removing from database. This will NOT disconnect
	 * the associated bot.
	 * @param address  The address of the server to be deleted
	 */
	public void removeServer(String address) {
		try {
			Collection<Server> c = dbm.loadObjects(new ArrayList<Server>(), Server.class);
			for (Server curServ : c)
				if (curServ.getAddress().equals(address))
					curServ.delete();
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
		HookManager.getList("onPluginLoadStart").execute();
		CommandManager.removeAll();

		mainPool.submit(new Runnable() {
			@Override
			public void run() {
				try {
					//Load all permanent commands
					reloadPlugins(new File("plugins"));
					HookManager.getList("onPluginLoadComplete").execute();
				} catch (Exception e) {
					log.error("Error in plugin loading!!!", e);
				}
			}
		});
	}

	/**
	 * Recusrivly load commands from current file. Use
	 * @param file
	 */
	private void reloadPlugins(File file) {
		String[] extArr = null;
		//Load using appropiate type
		try {
			if (file.isDirectory()) {
				final File[] childs = file.listFiles();
				for (File child : childs)
					reloadPlugins(child);
				return;
			} //Is this in the .svn directory?
			else if (file.getAbsolutePath().indexOf(".svn") != -1 || file.getName().equals("JS_Template.js"))
				return;

			//Get extension of file
			extArr = StringUtils.split(file.getName(), '.');
			if (extArr.length < 2)
				return;
			String ext = extArr[1];

			//Load with pluginType
			pluginLoaders.get(ext).load(file);
		} catch (Exception e) {
			log.error("Could not load plugin " + extArr[0], e);
		}
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extention
	 * @param ext     Exentsion to associate Command Type with
	 * @param newType Class of Command Type
	 */
	public static void addPluginLoader(PluginLoader loader, String ext) {
		addPluginLoader(loader, new String[]{ext});
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extentions
	 * @param exts     Extention to associate Command Type with
	 * @param newType Class of Command Type
	 */
	public static void addPluginLoader(PluginLoader loader, String[] exts) {
		for (String curExt : exts)
			pluginLoaders.put(curExt, loader);
	}

	/**
	 * Register a prefix that will activate bot
	 * @param prefix
	 */
	public void addPrefix(String prefix) {
		globPrefixes.add(prefix);
	}

	/**
	 * Set the log level of JPersist. By default its OFF, but can be changed for debugging
	 * @param level JUT logging level
	 */
	public void setDatabaseLogLevel(java.util.logging.Level level) {
		setLevel = level;
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
			Collection<Admin> c = dbm.loadObjects(new ArrayList<Admin>(), Admin.class, true);
			for (Admin curAdmin : c) {
				//Is this even a match?
				if (!curAdmin.getUser().equalsIgnoreCase(name))
					continue;

				//Is this person an admin of this channel?
				Channel chan = curAdmin.getChannel();
				if (chan != null && chan.getName().equals(channel))
					return true;

				//Is this person an admin of the server?
				Server serv = curAdmin.getServer();
				if (serv != null && serv.getAddress().equalsIgnoreCase(server))
					return true;

				//Is this person a global admin?
				if (serv != null && chan != null)
					return true;
			}
		} catch (JPersistException e) {
			log.error("Couldn't finish finding admin", e);
		}
		return false;
	}

	/**
	 * Create a DatabaseManager instance using a supplied DataSource.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize, dataSource, catalogPattern, schemaPattern);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param dataSource the data source that supplies connections
	 * @param catalogPattern the catalogPattern (can contain SQL wildcards)
	 * @param schemaPattern the schemaPattern (can contain SQL wildcards)
	 */
	public void connectDB(String databaseName, int poolSize, DataSource dataSource, String catalogPattern, String schemaPattern) {
		dbm = new DatabaseManager(databaseName, poolSize, dataSource, catalogPattern, schemaPattern);
	}

	/**
	 * Connect to Database using using JNDI.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize,jndiUri, catalogPattern, schemaPattern);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param jndiUri the JNDI URI
	 * @param catalogPattern the catalogPattern (can contain SQL wildcards)
	 * @param schemaPattern the schemaPattern (can contain SQL wildcards)
	 */
	public void connectDB(String databaseName, int poolSize, String jndiUri, String catalogPattern, String schemaPattern) {
		dbm = new DatabaseManager(databaseName, poolSize, jndiUri, catalogPattern, schemaPattern);
	}

	/**
	 * Create a DatabaseManager instance using JNDI.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize, jndiUri, catalogPattern, schemaPattern, username, password);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param jndiUri the JNDI URI
	 * @param catalogPattern the catalogPattern (can contain SQL wildcards)
	 * @param schemaPattern the schemaPattern (can contain SQL wildcards)
	 * @param username the username to use for signon
	 * @param password the password to use for signon
	 */
	public void connectDB(String databaseName, int poolSize, String jndiUri, String catalogPattern, String schemaPattern, String username, String password) {
		dbm = new DatabaseManager(databaseName, poolSize, jndiUri, catalogPattern, schemaPattern, username, password);
	}

	/**
	 * Create a DatabaseManager instance using a supplied database driver.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize, driver, url, catalogPattern, schemaPattern);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param driver the database driver class name
	 * @param url the driver oriented database url
	 * @param catalogPattern the catalogPattern (can contain SQL wildcards)
	 * @param schemaPattern the schemaPattern (can contain SQL wildcards)
	 */
	public void connectDB(String databaseName, int poolSize, String driver, String url, String catalogPattern, String schemaPattern) {
		dbm = new DatabaseManager(databaseName, poolSize, driver, url, catalogPattern, schemaPattern);
	}

	/**
	 * Create a DatabaseManager instance using a supplied database driver.
	 * <p>
	 * This simply calls
	 * <code>dbm = new DatabaseManager(databaseName, poolSize, driver, url, catalogPattern, schemaPattern, username, password);</code>
	 *
	 * @param databaseName the name to associate with the DatabaseManager instance
	 * @param poolSize the number of instances to manage
	 * @param driver the database driver class name
	 * @param url the driver oriented database url
	 * @param catalogPattern the catalogPattern (can contain SQL wildcards)
	 * @param schemaPattern the schemaPattern (can contain SQL wildcards)
	 * @param username the username to use for signon
	 * @param password the password to use for signon
	 */
	public void connectDB(String databaseName, int poolSize, String driver, String url, String catalogPattern, String schemaPattern, String username, String password) {
		dbm = new DatabaseManager(databaseName, poolSize, driver, url, catalogPattern, schemaPattern, username, password);
	}
}
