/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.Iterator;
import org.quackbot.hooks.HookLoader;
import java.util.Set;
import org.quackbot.gui.GUI;
import org.quackbot.hooks.HookManager;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.ServerStore;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.quackbot.data.DataStore;
import org.quackbot.events.InitEvent;
import org.quackbot.events.HookLoadEndEvent;
import org.quackbot.events.HookLoadEvent;
import org.quackbot.events.HookLoadStartEvent;
import org.quackbot.hooks.Hook;
import org.quackbot.hooks.loaders.JSHookLoader;
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
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@Setter(AccessLevel.NONE)
@EqualsAndHashCode(exclude = {"bots"})
@Slf4j
public class Controller {
	protected final DataStore storage;
	/**
	 * Set of all Bot instances
	 */
	protected HashSet<Bot> bots = new HashSet<Bot>();
	/**
	 * Number of Commands executed, used by logging
	 */
	protected int commandNumber = 0;
	protected GUI gui;
	protected HookManager hookManager = new HookManager();
	/**
	 * Global Prefixes.
	 */
	protected List<String> prefixes = Collections.synchronizedList(new ArrayList<String>());
	/**
	 * All registered plugin types
	 */
	private TreeMap<String, HookLoader> pluginLoaders = new TreeMap<String, HookLoader>();
	@Setter(AccessLevel.PUBLIC)
	private String version = "";
	@Setter(AccessLevel.PUBLIC)
	private String finger = "";
	private final String suffix = "Quackbot Java IRC Framework 3.3 http://quackbot.googlecode.com/";
	@Getter(AccessLevel.NONE)
	protected boolean createGui = true;
	@Setter(AccessLevel.PUBLIC)
	protected int defaultPort = 6667;
	@Setter(AccessLevel.PUBLIC)
	protected String defaultName = "QuackbotUser";
	@Setter(AccessLevel.PUBLIC)
	protected String defaultLogin = "QB";
	@Setter(AccessLevel.PUBLIC)
	protected int defaultMessageDelay = 1750;

	/**
	 * Init for Quackbot. Sets instance, adds shutdown hook, and starts GUI if requested
	 * @param makeGui  Show the GUI or not. WARNING: If there is no GUI, a slf4j Logging
	 *                 implementation <b>must</b> be provided to get any outpu
	 */
	public Controller(DataStore storage) {
		this.storage = storage;

		//Add shutdown hook to kill all bots and connections
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Logger log = LoggerFactory.getLogger(this.getClass());
				log.info("Closing all IRC and db connections gracefully");

				Controller.this.stopAll();
				try {
					Controller.this.getStorage().close();
				} catch (Exception e) {
					e.printStackTrace(); //send to standard output because window is closing
				}
			}
		});

		//Do we need to make a GUI?
		if (isGuiCreated())
			try {
				//This can't run in EDT, end if it is
				if (SwingUtilities.isEventDispatchThread()) {
					log.error("Controller cannot be started from EDT. Please start from seperate thread");
					return;
				}

				gui = new GUI(this);
			} catch (Exception e) {
				log.error("Unkown error occured in GUI initialzation", e);
			}

		//Get ahold of ControlAppender and set this as the Controller
		ch.qos.logback.classic.Logger rootLog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
		boolean noAppender = true;
		for (Iterator<Appender<ILoggingEvent>> appenderItr = rootLog.iteratorForAppenders(); appenderItr.hasNext();) {
			Appender<ILoggingEvent> curAppender = appenderItr.next();
			if (curAppender instanceof ControlAppender) {
				((ControlAppender) curAppender).setController(this);
				noAppender = false;
			}
		}
		
		//Display a large error message if there a GUI but no appender
		if(noAppender) {
			gui.CerrorLog.setText("ERROR: ControlAppender hasn't been added as a logger! This GUI will not display any log messages");
			gui.CerrorLog.setForeground(Color.red);
		}

		//Setup default Plugin Loaders
		addPluginLoader(new JSHookLoader(), "js");
	}

	/**
	 * Executes Quackbot. Loads commands, starts service commands, connects to servers.
	 * If this isn't called, then the bot does nothing
	 */
	public void start() {
		//Call list of commands
		getHookManager().dispatchEvent(new InitEvent(this));

		//Load current CMD classes
		reloadPlugins();

		if (true)
			return;

		//Connect to all servers
		try {
			Set<ServerStore> servers = storage.getServers();
			if (servers.isEmpty())
				log.error("Server list is empty!");
			for (ServerStore curServer : servers)
				initBot(curServer);
		} catch (Exception e) {
			log.error("Error encountered while attempting to join servers", e);
		}
	}

	/**
	 * Reloads all commands, clearing list only if requested
	 * @param clean Clear list of commands?
	 */
	public void reloadPlugins() {
		getHookManager().dispatchEvent(new HookLoadStartEvent(this));

		try {
			//Load all permanent commands
			reloadPlugins(new File("plugins"));
			getHookManager().dispatchEvent(new HookLoadEndEvent(this));
		} catch (Exception e) {
			log.error("Error in plugin loading!!!", e);
		}
	}

	/**
	 * Recursively load commands from current file. Use
	 * @param file
	 */
	protected void reloadPlugins(File file) {
		String[] extArr = null;
		HookLoader loader = null;
		Hook hook = null;
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
			loader = getPluginLoaders().get(ext);
			if (loader != null)
				hook = loader.load(file.getAbsolutePath());
			getHookManager().dispatchEvent(new HookLoadEvent(this, hook, loader, file, null));
		} catch (Exception e) {
			log.error("Could not load plugin " + extArr[0], e);
			getHookManager().dispatchEvent(new HookLoadEvent(this, hook, loader, file, e));
		}
	}

	/**
	 * Starts bot using server object
	 * @param curServer
	 */
	public void initBot(final ServerStore curServer) {
		//Build a thread pool for the bot
		final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
			int threadCounter = 0;
			List<String> usedNames = new ArrayList<String>();
			ThreadGroup threadGroup;

			@Override
			public Thread newThread(Runnable rbl) {
				String address = curServer.getAddress();
				String goodAddress = address;

				int counter = 0;
				while (usedNames.contains(goodAddress))
					goodAddress = address + "-" + (counter++);

				if (threadGroup == null)
					threadGroup = new ThreadGroup("quackbot-" + goodAddress);
				return new Thread(threadGroup, rbl, "quackbot-" + goodAddress + "-" + threadCounter++);
			}
		});

		//Execute bot in its thread Pool
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Initiating IRC connection to server " + curServer);
					Bot bot = new Bot(Controller.this, curServer.getServerId(), threadPool);
					bot.setVerbose(true);
					bots.add(bot);
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
		for (Bot curBot : bots) {
			curBot.quitServer("Killed by control panel");
			curBot.dispose();
		}
		bots.clear();
		log.info("Killed all bots");
	}

	public void addServer(String address, String... channels) {
		addServer(address, getDefaultPort(), channels);
	}

	/**
	 * Creates a new server, adds to database, and connects
	 * @param address  Address of server
	 * @param port     Port number to be used (if null, the 6667 is used)
	 * @param channels Vararg of channels to join
	 */
	public void addServer(String address, int port, String... channels) {
		ServerStore server = getStorage().newServerStore(address);
		server.setPort(port);
		for (String curChan : channels)
			server.addChannel(getStorage().newChannelStore(curChan));
		initBot(server);
	}

	/**
	 * Deletes a server by address name, removing from database. This will NOT disconnect
	 * the associated bot. <b>Warning:</b> If you have multiple bots on one server
	 * this will delete <u>all</u> of them. 
	 * @param address  The address of the server to be deleted
	 */
	public void removeServer(String address) {
		try {
			for (ServerStore curServ : storage.getServers())
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
		for (Bot curBot : bots)
			curBot.sendAllMessage(msg);
	}

	/**
	 * Increments command number and returns new int
	 */
	public synchronized int addCommandNumber() {
		return ++commandNumber;
	}

	public boolean isAdmin(Bot bot, User user, Channel chan) {
		for (AdminStore curAdmin : storage.getAllAdmins()) {
			//Is this even the right user?
			if (!curAdmin.getName().equalsIgnoreCase(user.getNick()))
				continue;

			//Got our user; are they an admin on this server?
			if (curAdmin.getServers().contains(bot.getServerStore()))
				return true;

			//Are they an admin on the channel?
			for (ChannelStore curChan : curAdmin.getChannels())
				if (curChan.getName().equalsIgnoreCase(chan.getName()))
					return true;

			//They aren't an admin, end
			break;
		}

		//Loop failed, they aren't an admin
		return false;
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extention
	 * @param ext     Exentsion to associate Command Type with
	 * @param newType Class of Command Type
	 */
	public void addPluginLoader(HookLoader loader, String ext) {
		addPluginLoader(loader, new String[]{ext});
	}

	/**
	 * Register a custom plugin type with Quackbot, associating with the specified extentions
	 * @param exts     Extention to associate Command Type with
	 * @param newType Class of Command Type
	 */
	public void addPluginLoader(HookLoader loader, String[] exts) {
		for (String curExt : exts)
			getPluginLoaders().put(curExt, loader);
	}

	public boolean addPrefix(String prefix) {
		return prefixes.add(prefix);
	}

	public boolean removePrefix(String prefix) {
		return prefixes.add(prefix);
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		String output = "";
		if (StringUtils.isNotBlank(version))
			output = version + " - ";
		return output + suffix;
	}

	/**
	 * @return the finger
	 */
	public String getFinger() {
		String output = "";
		if (StringUtils.isNotBlank(finger))
			output = finger + " - ";
		return output + suffix;
	}

	public void createGui(boolean createGui) {
		this.createGui = createGui;
	}

	public boolean isGuiCreated() {
		return createGui;
	}
}
