/**
 * @(#)Controller.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.Admin;
import Quackbot.info.Channel;
import Quackbot.info.Server;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;
import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;

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
	public HashSet<Bot> bots = new HashSet<Bot>();
	public ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	public ExecutorService threadPool = Executors.newCachedThreadPool();
	public ExecutorService threadPool_js = Executors.newCachedThreadPool();
	public Main gui;
	public Session JRSession = null;
	public Node JRRoot = null;
	public ObjectContentManager JRocm = null;

	/**
	 * Start of bot working. Loads CMDs and starts Bots
	 * @param gui   Current GUI
	 */
	public Controller(Main gui) {
		//Add GUI to instance vars
		this.gui = gui;

		//Load current CMD classes
		//reloadCMDs();

		//Connect to JackRabbit DB and join servers
		try {
			//Connect to server
			System.out.println("Starting JackRabbit...");
			JRSession = new TransientRepository().login(new SimpleCredentials("empty", "really??".toCharArray()));
			System.out.println("JackRabbit started!");

			//Setup variables
			JRRoot = JRSession.getRootNode();
			List<Class> classes = new ArrayList<Class>();
			classes.add(Server.class);
			classes.add(Channel.class);
			classes.add(Admin.class);
			Mapper mapper = new AnnotationMapperImpl(classes);
			JRocm = new ObjectContentManagerImpl(JRSession, mapper);

			//recursiveShow(JRSession.getRootNode());

			// Retrieve server info and join them
			NodeIterator node = JRRoot.getNode("servers").getNodes();
			while(node.hasNext()) {
				Node curNode = node.nextNode();
				Server curServer = (Server)JRocm.getObject(curNode.getPath());
				threadPool.execute(new botThread(curServer.getAddress(),curServer.getChannels()));
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		JRSession.logout();
	}

	public void addServer(String address, String... channels) {
		Server newServ = new Server();
		for(String chan : channels)
			newServ.addChannel(new Channel(chan));
		newServ.setPath("/servers/"+address);
		JRocm.insert(newServ);
		JRocm.save();
		threadPool.execute(new botThread(newServ.getAddress(),newServ.getChannels()));
	}

	public void removeServer(String address) {
		try {
			JRRoot.getNode("server/"+address).remove();
			JRSession.save();
		}
		catch(Exception e) {
			e.printStackTrace();
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
		threadPool.execute(new loadCMDs(this));
	}

	/**
	 * Simple thread to run the bot in to prevent it from locking the gui
	 */
	public class botThread implements Runnable {

		String server = null;
		List<Channel> channels = null;

		/**
		 * Define some simple variables
		 * @param server
		 * @param channels
		 */
		public botThread(String server, List<Channel> channels) {
			this.server = server;
			this.channels = channels;
		}

		/**
		 * Initiates bot, joins it to some channels
		 */
		public void run() {
			try {
				System.out.println("Initiating IRC connection");
				Bot qb = new Bot(Controller.this);
				qb.setVerbose(true);
				qb.connect(server, 6665);
				Iterator chanItr = channels.iterator();
				while(chanItr.hasNext()) {
					Channel curChan = (Channel)chanItr.next();
					String channel= curChan.getName();
					qb.joinChannel(channel);
					System.out.println("Channel: " + channel);
				}
				bots.add(qb);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * DEBUG: Displays all paths that exist in database
	 * @param node
	 */
	public void recursiveShow(Node node) {
		try {
			String nodePath = node.getPath();
			if(nodePath.indexOf("jcr:") != -1)
				return;
			System.out.println();
			if (node.hasNodes()) {
				NodeIterator nodeItr = node.getNodes();
				while (nodeItr.hasNext()) {
					recursiveShow(nodeItr.nextNode());
				}
			}
			if(node.hasProperties()) {
				PropertyIterator propItr = node.getProperties();
				while(propItr.hasNext()) {
					Property curProp = propItr.nextProperty();
					if(curProp.getPath().indexOf("jcr:") != -1)
						continue;
					System.out.println(curProp.getPath()+" = "+curProp.getString());
				}
					
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
