/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 *   -javaagent:lib/jrebel.jar -Drebel.log4j-plugin=true -noverify
 *
 */
package Quackbot;

import Quackbot.info.Channel;
import Quackbot.info.Server;
import Quackbot.log.ControlAppender;
import Quackbot.plugins.JSPlugin;
import Quackbot.plugins.JavaPlugin;
import Quackbot.plugins.core.Help;
import Quackbot.plugins.core.JavaTest;
import java.io.File;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import javax.swing.SwingUtilities;


import jpersist.DatabaseManager;
import jpersist.JPersistException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * Global Prefixes.
	 */
	public List<String> globPrefixes = new ArrayList<String>();
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
	public List<AppenderSkeleton> ctrlAppenders = new ArrayList<AppenderSkeleton>();
	public List<AppenderSkeleton> botAppenders = new ArrayList<AppenderSkeleton>();
	private boolean setLevel = false;

	/**
	 * Calls {@link #Controller(java.util.List, boolean)}
	 * with empty custom prefixes and makeGui set to true
	 */
	public Controller() {
		this(true);
	}

	/**
	 * Main init, restarts this in main thread pool, starts GUI if requested
	 * <p>
	 * Private constructor that this calls completly sets up bot. Connects to
	 * database, adds built in PluginTypes, starts all bots from database
	 * @param makeGui  Show the GUI or not. If there is no GUI, then all output is directed
	 *                 to the console
	 */
	public Controller(boolean makeGui) {
		InstanceTracker.setController(this);

		//Setup log4j for quackbot package
		ctrlAppenders.add(new ControlAppender());

		//Logger rootLog = LoggerFactory.getLogger("Quackbot");
		org.apache.log4j.Logger rootLog = org.apache.log4j.Logger.getRootLogger();
		rootLog.setLevel(Level.ALL);
		for (AppenderSkeleton curAppender : ctrlAppenders)
			rootLog.addAppender(curAppender);

		//Add shutdown hook to kill all bots and connections
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Logger log = LoggerFactory.getLogger(this.getClass());
				log.info("Closing all IRC and db connections gracefully");
				Controller ctrl = InstanceTracker.getController();
				ctrl.stopAll();
				try {
					ctrl.dbm.close();
				} catch (NullPointerException e) {
				} catch (Exception e) {
					e.printStackTrace(); //send to standard output because window is closing
				}
			}
		});

		//This can't run in EDT, end if it is
		if (SwingUtilities.isEventDispatchThread()) {
			log.error("Controller cannot be started from EDT. Please start from seperate thread");
			return;
		}

		//Start GUI if requested
		try {
			if (makeGui)
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						new GUI();
					}
				});
		} catch (Exception e) {
			log.error("Cannot start GUI", e);
		}

		//Setup plugin loading
		addPluginType("js", JSPlugin.class);
		addPluginType("java", JavaPlugin.class);

		//Add basic plugin
		addPlugin(new JavaPlugin(Help.class.getName()));
	}

	public void start() {
		if(dbm==null) {
			log.error("Not configured to use database! Must run connectDB ");
			return;
		}

		if(!setLevel)
			DatabaseManager.setLogLevel(java.util.logging.Level.OFF);

		//Load current CMD classes
		reloadPlugins(false);

		//Start service plugins
		for (PluginType curPlug : plugins)
			if (curPlug.isService())
				new PluginExecutor(curPlug.getName(), new String[0]);

		//Get all server objects from database
		Collection<Server> c = null;
		try {
			c = dbm.loadObjects(new ArrayList<Server>(), Server.class, true);
			if (c.size() == 0)
				log.error("Server list is empty!");
			for (final Server curServer : c) {
				dbm.loadAssociations(c);
				final ExecutorService threadPool = ThreadMgr.newBotPool(curServer.getAddress());
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
	 * Creates a new server, adds to database, and joins
	 * @param address  Address of server
	 * @param port     Port number to be used (if null, the 6667 is used)
	 * @param channels Vararg of channels to join
	 */
	public void addServer(String address, int port, String... channels) {
		Server srv = new Server(address, 6667);
		for (String curChan : channels)
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
			for (Server curServ : c)
				if (curServ.getAddress().equals(address))
					curServ.delete();
		} catch (Exception e) {
			log.error("Can't remove server", e);
		}
	}

	/**
	 * Send a message to every channel on every server the bot is connected to
	 * @param msg   Message to send
	 */
	public void sendGlobalMessage(String msg) {
		for (Bot curBot : bots.values())
			curBot.sendAllMessage(msg);
	}

	/**
	 * Reload all plugins
	 *
	 * Note: This does shutdown all the thread pools (Bot instances are unaffected).
	 *	Take this into account if you have services running in the background
	 */
	public void reloadPlugins() {
		reloadPlugins(false);
	}

	public void reloadPlugins(boolean clean) {
		if (clean)
			plugins.removeAll(plugins);
		ThreadMgr.addMain(new Runnable() {
			public void run() {
				reloadPlugins(new File("plugins"));
			}
		});
	}

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
		String ext = StringUtils.split(file.getName(), '.')[1];

		//Load using appropiate type
		try {
			PluginType plugin = pluginTypes.get(ext).newInstance();
			plugin.load(file);
			if (plugin.getName() != null)
				plugins.add(plugin);
		} catch (Exception e) {
			log.error("Could not load plugin " + StringUtils.split(file.getName(), '.')[0], e);
		}
	}

	public void addPluginType(String ext, Class<? extends PluginType> newType) {
		addPluginType(new String[]{ext}, newType);
	}

	public void addPluginType(String[] exts, Class<? extends PluginType> newType) {
		for (String curExt : exts)
			pluginTypes.put(curExt, newType);
	}

	public void addPlugin(PluginType plugin) {
		plugins.add(plugin);
	}

	public void addPrefix(String prefix) {
		globPrefixes.add(prefix);
	}

	public void addMainAppender() {
	}

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

	public static void main(String[] args) {
		Controller ctrl = new Controller();
		ctrl.connectDB("quackbot", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/quackbot", null, null, "root", null);
		ctrl.setDatabaseLogLevel(java.util.logging.Level.OFF);
		ctrl.addPlugin(new JavaPlugin(JavaTest.class.getName()));
		//ctrl.addPlugin(new JavaPlugin(HookTest.class.getName()));
		ctrl.start();
	}
}
