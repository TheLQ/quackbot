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

import com.google.common.util.concurrent.Service;
import org.quackbot.hooks.HookLoader;
import org.quackbot.gui.GUI;
import org.quackbot.hooks.HookManager;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.ServerDAO;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.SwingUtilities;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.quackbot.dao.LogDAO;
import org.quackbot.dao.model.AdminEntry;
import org.quackbot.dao.model.ServerEntry;
import org.quackbot.hooks.events.InitEvent;
import org.quackbot.hooks.events.HookLoadEndEvent;
import org.quackbot.hooks.events.HookLoadEvent;
import org.quackbot.hooks.events.HookLoadStartEvent;
import org.quackbot.hooks.CommandManager;
import org.quackbot.hooks.QListener;
import org.quackbot.hooks.core.CoreQuackbotListener;
import org.quackbot.hooks.core.HelpCommand;
import org.quackbot.hooks.core.QuackbotLogHook;
import org.quackbot.hooks.loaders.JavaHookLoader;
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
@ToString
@EqualsAndHashCode(exclude = {"bots"})
@Slf4j
@Getter
public class Controller {
	protected final QConfiguration qconfiguration;
	protected final AdminDAO adminDao;
	protected final ChannelDAO channelDao;
	protected final LogDAO logDao;
	protected final ServerDAO serverDao;
	/**
	 * Set of all Bot instances
	 */
	protected final LinkedHashMap<Integer, Bot> bots = new LinkedHashMap<Integer, Bot>();
	protected final Thread shutdownHook;
	/**
	 * Number of Commands executed, used by logging
	 */
	protected int commandNumber = 0;
	protected GUI gui;
	protected final HookManager hookManager = new HookManager();
	protected final CommandManager commandManager;
	protected final QMultiBotManager botManager = new QMultiBotManager();
	/**
	 * All registered plugin loaders
	 */
	protected Service.State state = Service.State.NEW;

	/**
	 * Init for Quackbot. Sets instance, adds shutdown hook, and starts GUI if requested
	 * @param makeGui  Show the GUI or not. WARNING: If there is no GUI, a slf4j Logging
	 *                 implementation <b>must</b> be provided to get any output
	 */
	public Controller(QConfiguration qconfiguration) {
		this.qconfiguration = qconfiguration;
		this.adminDao = qconfiguration.getDaoFactory().createAdminDAO();
		this.channelDao = qconfiguration.getDaoFactory().createChannelDAO();
		this.logDao = qconfiguration.getDaoFactory().createLogDAO();
		this.serverDao = qconfiguration.getDaoFactory().createServerDAO();
		this.commandManager = new CommandManager(qconfiguration);

		//Add core hooks
		try {
			hookManager.addListener(new CoreQuackbotListener());
			hookManager.addListener(new QuackbotLogHook());
			JavaHookLoader.loadCommands(commandManager, new HelpCommand());
		} catch (Exception e) {
			log.error("Could not load core hooks", e);
		}

		//Add shutdown hook to kill all bots and connections
		Runtime.getRuntime().addShutdownHook(shutdownHook = new Thread() {
			@Override
			public void run() {
				LoggerFactory.getLogger(this.getClass()).info("JVM shutting down, closing all IRC connections gracefully");
				try {
					Controller.this.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Executes Quackbot. Loads commands, starts service commands, connects to servers.
	 * If this isn't called, then the bot does nothing
	 */
	public void start() {
		if (state != Service.State.NEW)
			throw new RuntimeException("Can't run start more than once");
		state = Service.State.STARTING;

		//Call list of commands
		getHookManager().dispatchEvent(new InitEvent(this));

		//Load current CMD classes
		reloadPlugins();

		List<ServerEntry> servers = serverDao.findAll();
		if (servers.isEmpty())
			throw new RuntimeException("Server list is empty!");
		for (ServerEntry curServer : servers)
			botManager.addBot(curServer);
		botManager.start();
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
		QListener hook = null;

		//Ignore files or folders that start with a period
		if (file.getName().startsWith("."))
			return;

		//Load using appropiate type
		try {
			if (file.isDirectory()) {
				final File[] childs = file.listFiles();
				for (File child : childs)
					reloadPlugins(child);
				return;
			}

			//Get extension of file
			extArr = StringUtils.split(file.getName(), '.');
			if (extArr.length < 2)
				return;
			String ext = extArr[1];

			//Load with pluginType
			loader = qconfiguration.getHookLoaders().get(ext);
			if (loader != null)
				hook = loader.load(file.getAbsolutePath());
			getHookManager().dispatchEvent(new HookLoadEvent(this, hook, loader, file, null));
		} catch (Exception e) {
			log.error("Could not load plugin " + extArr[0], e);
			getHookManager().dispatchEvent(new HookLoadEvent(this, hook, loader, file, e));
		}
	}

	/**
	 * Makes all bots quit servers
	 */
	public void shutdown() throws InterruptedException {
		botManager.stopAndWait();
		state = Service.State.STOPPING;
		if (!shutdownHook.isAlive())
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		bots.clear();
		log.info("Killed all bots");
		state = Service.State.TERMINATED;
	}

	/**
	 * Creates a new server, adds to database, and connects
	 * @param address  Address of server
	 * @param port     Port number to be used (if null, the 6667 is used)
	 * @param channels Vararg of channels to join
	 */
	public void addServer(ServerEntry serverEntry) {
		serverDao.save(serverEntry);
		botManager.addBot(serverEntry);
	}

	/**
	 * Deletes a server by address name, removing from database. This will NOT disconnect
	 * the associated bot. <b>Warning:</b> If you have multiple bots on one server
	 * this will delete <u>all</u> of them. 
	 * @param address  The address of the server to be deleted
	 */
	public void removeServer(ServerEntry serverEntry) {
		serverDao.delete(serverEntry);
		botManager.removeBot(serverEntry);
	}

	/**
	 * Send a message to every channel on every server Quackbot is connected to. Use carefully!
	 * @param msg   Message to send
	 */
	public void sendGlobalMessage(String msg) {
		for (Bot curBot : botManager.getBots())
			curBot.sendAnnounceMessage(msg);
	}

	/**
	 * Increments command number and returns new int
	 */
	public synchronized int addCommandNumber() {
		return ++commandNumber;
	}

	public boolean isAdmin(Bot bot, User user, Channel chan) {
		AdminEntry admin = adminDao.findByName(user.getNick());

		//Null means not found
		if (admin == null)
			return false;

		//Are they a server admin?
		if (admin.getServers().contains(bot.getServerEntry()))
			return true;

		//Are they a channel admin?
		if (admin.getChannels().contains(channelDao.findByName(bot.getServerEntry(), chan.getName())))
			return true;

		//Getting here means they aren't an admin
		return false;
	}

	public void setGuiCreated(boolean guiCreated) {
		//Fail early if the value isn't being updated. Prevents GUI being created twice
		if (this.guiCreated == guiCreated)
			return;
		this.guiCreated = guiCreated;
		if (!guiCreated)
			return;

		//Need to create GUI
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
	}
}