/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

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
 * Other methods of intrest include {@link #addPlugin(Quackbot.PluginType)}, {@link #addPrefix(java.lang.String) },
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
	public TreeMap<String, Class<? extends PluginType>> pluginTypes = new TreeMap<String, Class<? extends PluginType>>();
	/**
	 * List of ALL commands (Java or JS)
	 */
	public List<PluginType> plugins = new ArrayList<PluginType>();
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
	private boolean setLevel = false;
	/**
	 * A list of Hooks that will be executed first during {@link #start()}.
	 * <p>
	 * Example of execution
	 * <br>
	 * <code>
	 * static {
	 *	Controller.initHooks.add(new InitHook() {
	 *		public void run(Controller ctrl) {
	 *			//do something
	 *		}
	 *	});
	 * }
	 * </code>
	 *
	 * @see InitHook
	 */
	public static final List<InitHook> initHooks = new ArrayList<InitHook>();
	/**
	 * ThreadPool that all non-bot threads are executed in
	 */
	public static final ExecutorService mainPool = Executors.newCachedThreadPool();

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
				e.printStackTrace();
			}
		}
	}

	/**
	 * Executues Quackbot. Loads plugins, starts service plugins, connects to servers.
	 * If this isn't called, then the bot does nothing
	 */
	public void start() {
		if (dbm == null) {
			log.error("Not configured to use database! Must run connectDB ");
			return;
		}

		if (!setLevel)
			DatabaseManager.setLogLevel(java.util.logging.Level.OFF);

		//Call list of commands
		synchronized (initHooks) {
			for (InitHook curHook : initHooks)
				curHook.run(this);
		}

		//Load current CMD classes
		reloadPlugins(false);

		//Start service plugins
		for (PluginType curPlug : plugins)
			if (curPlug.isService())
				new PluginExecutor(curPlug.getName(), new String[0]);

		//Connect to all servers
		try {
			Collection<Server> c = dbm.loadObjects(new ArrayList<Server>(), Server.class, true);
			if (c.size() == 0)
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
	 * Reload all plugins, clearing the list
	 */
	public void reloadPlugins() {
		reloadPlugins(true);
	}

	/**
	 * Reloads all plugins, clearing list only if requested
	 * @param clean Clear list of plugins?
	 */
	public void reloadPlugins(boolean clean) {
		log.trace("Reload called with " + clean);
		if (clean)
			plugins.removeAll(plugins);
		log.trace("Plugins: " + pluginTypes);
		mainPool.submit(new Runnable() {
			public void run() {
				reloadPlugins(new File("plugins"));
				reloadPlugins(new File("Quackbot-Impl/plugins"));
			}
		});
	}

	/**
	 * Recusrivly load plugins from current file. Use
	 * @param file
	 */
	private void reloadPlugins(File file) {
		if (file.isDirectory()) {
			final File[] childs = file.listFiles();
			for (File child : childs)
				reloadPlugins(child);
			return;
		} //Is this in the .svn directory?
		else if (file.getAbsolutePath().indexOf(".svn") != -1 || file.getName().equals("JS_Template.js"))
			return;

		//Get extension of file
		String[] extArr = StringUtils.split(file.getName(), '.');
		if (extArr.length < 2)
			return;
		String ext = extArr[1];

		//Load using appropiate type
		try {
			Class<? extends PluginType> pluginType = pluginTypes.get(ext);
			if (pluginType == null)
				return;
			PluginType plugin = pluginType.newInstance();
			plugin.load(file);
			if (plugin.getName() != null)
				plugins.add(plugin);
		} catch (Exception e) {
			log.error("Could not load plugin " + StringUtils.split(file.getName(), '.')[0], e);
		}
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extention
	 * @param ext     Exentsion to associate Plugin Type with
	 * @param newType Class of Plugin Type
	 */
	public void addPluginType(String ext, Class<? extends PluginType> newType) {
		addPluginType(new String[]{ext}, newType);
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extentions
	 * @param exts     Exentsions to associate Plugin Type with
	 * @param newType Class of Plugin Type
	 */
	public void addPluginType(String[] exts, Class<? extends PluginType> newType) {
		for (String curExt : exts)
			pluginTypes.put(curExt, newType);
	}

	/**
	 * Add a plugin to Bot
	 * @param plugin Implementation of PluginType
	 */
	public void addPlugin(PluginType plugin) {
		plugins.add(plugin);
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
		DatabaseManager.setLogLevel(level);
		setLevel = true;
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

	/**
	 * Find a plugin by name
	 * @param name Name of plugin
	 * @return     Plugin Object or null if not found
	 */
	public PluginType findPlugin(String name) {
		List<PluginType> slist = Controller.instance.plugins;
		for (PluginType curItem : slist)
			if (curItem.getName().equalsIgnoreCase(name))
				return curItem;
		return null;
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