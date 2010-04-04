/**
 * @(#)loadCMDs.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *  Load (or reload) all CMD classes
 *  -Dump into controller instance
 * @author Lord.Quackstar
 */
public class loadCMDs implements Runnable {

	Controller ctrl = null;
	TreeSet<String> newCMDs = new TreeSet<String>();
	TreeSet<String> updatedCMDs = new TreeSet<String>();
	TreeSet<String> deletedCMDs = new TreeSet<String>();
	TreeMap<String, TreeMap<String, Object>> cmdBack;
	TreeMap<String, TreeMap<String, Object>> listenerBack;
	TreeMap<String, TreeMap<String, Object>> servicesBack;
	public TreeSet<String> utilsBack;
	String curFile = "";
	Logger log = Logger.getLogger(loadCMDs.class);

	/**
	 * Make known Controller instance
	 * @param ctrl  Controller instance
	 */
	public loadCMDs(Controller ctrl) {
		this.ctrl = ctrl;
	}

	/**
	 * Initate recursive scan in seperate thread. Reports to bots any updates
	 */
	public void run() {
		cmdBack = new TreeMap<String, TreeMap<String, Object>>(ctrl.cmds);
		listenerBack = new TreeMap<String, TreeMap<String, Object>>(ctrl.listeners);
		servicesBack = new TreeMap<String, TreeMap<String, Object>>(ctrl.services);
		utilsBack = new TreeSet<String>(ctrl.utils);
		try {
			ctrl.cmds.clear();
			ctrl.listeners.clear();
			ctrl.services.clear();
			ctrl.utils.clear();
			ctrl.threadPool_js.shutdownNow();
			ctrl.threadPool_js = Executors.newCachedThreadPool();
			File cmddir = new File("js");
			if (!cmddir.exists()) {
				log.fatal("CMD directory not found! CD: " + new File(".").getAbsolutePath());
				return;
			}

			//Call recursive file method
			traverse(cmddir);

			log.debug("cmdBack len: " + cmdBack.size() + " | cmds len: " + ctrl.cmds.size());

			//Get deleted CMDs
			Iterator cmdItr = cmdBack.keySet().iterator();
			while (cmdItr.hasNext()) {
				String curCmd = (String) cmdItr.next();
				if (!ctrl.cmds.containsKey(curCmd) && !newCMDs.contains(curCmd)) {
					deletedCMDs.add(curCmd);
				}
			}

			String newStr = (newCMDs.size() == 0) ? "" : "New: " + StringUtils.join(newCMDs.toArray(), ", ");
			String updateStr = (updatedCMDs.size() == 0) ? "" : "Updated: " + StringUtils.join(updatedCMDs.toArray(), ", ");
			String delStr = (deletedCMDs.size() == 0) ? "" : "Deleted: " + StringUtils.join(deletedCMDs.toArray(), ", ");

			//Notify everyone of new change
			if ((newStr + updateStr + delStr).equals("")) {
				log.info("Reload changed nothing!");
				return;
			}
			ctrl.sendGlobalMessage("Bot commands reloaded! " + newStr + " " + updateStr + " " + delStr);

			//Start services
			Iterator servItr = ctrl.services.entrySet().iterator();
			while (servItr.hasNext()) {
				TreeMap<String, Object> servVal = ((Map.Entry<String, TreeMap<String, Object>>) servItr.next()).getValue();
				log.debug("Excecuting service");
				ctrl.threadPool_js.execute(new threadCmdRun("invoke();", (ScriptContext) servVal.get("context"), ctrl));
			}

		} catch (Exception e) {
			ctrl.cmds = cmdBack;
			log.error("Error in reload, reverting to cmd backup. Last file: " + this.curFile, e);
		}
		return;
	}

	/**
	 * Recursive method that transverses CMD directory. All .svn directories are ignored
	 * @param file        Directory to scan
	 * @throws Exception  Errors that might be a result from File issues, parsing issues, or misc.
	 */
	public void traverse(File file) throws Exception {
		if (file.isDirectory()) {
			final File[] childs = file.listFiles();
			for (File child : childs) {
				traverse(child);
			}
			return;
		} //Is this in the .svn directory?
		else if (file.getAbsolutePath().indexOf(".svn") != -1 || file.getName().equals("JS_Template.js")) {
			return;
		}

		//Basic setup
		ScriptEngine jsEngine = ctrl.jsEngine;
		String name = StringUtils.split(file.getName(), ".")[0];

		this.curFile = name;

		//Read File Line By Line
		BufferedReader input = new BufferedReader(new FileReader(file));
		StringBuilder fileContents = new StringBuilder();
		String strLine;
		while ((strLine = input.readLine()) != null) {
			fileContents.append(strLine + System.getProperty("line.separator"));
		}
		input.close();
		String contents = fileContents.toString();

		//Make new context
		ScriptContext newContext = new SimpleScriptContext();
		Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
		engineScope.put("ctrl", ctrl);
		jsEngine.eval(contents, newContext);

		//JS info
		boolean isAdmin = ((engineScope.get("admin") == null) ? false : true);
		boolean isService = ((engineScope.get("service") == null) ? false : true);
		boolean isListener = ((engineScope.get("listener") == null) ? false : true);
		boolean isIgnore = ((engineScope.get("ignore") == null) ? false : true);
		boolean isUtil = ((engineScope.get("util") == null) ? false : true);

		//Method update list: Is this an existing method?
		if (isListener) {
			updateChanges(listenerBack, name, contents);
		} else if (isService) {
			updateChanges(servicesBack, name, contents);
		} else if (isUtil) {
			//Customized search due to theis being a TreeSet
			if (!utilsBack.contains(fileContents.toString())) {
				updatedCMDs.add(name);
			}
		} else {
			updateChanges(cmdBack, name, contents);
		}
		//Make a treemap containing very detailed info and dump into main map
		TreeMap<String, Object> cmdinfo = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		cmdinfo.put("src", fileContents.toString());
		cmdinfo.put("help", (String) engineScope.get("help"));
		cmdinfo.put("admin", isAdmin);
		cmdinfo.put("ReqArg", ((engineScope.get("ReqArg") == null) ? false : true));
		if (!isListener && !isService && !isUtil) {
			cmdinfo.put("param", (int) Double.parseDouble(engineScope.get("param").toString()));
		}
		cmdinfo.put("ignore", isIgnore);
		cmdinfo.put("context", newContext);
		cmdinfo.put("scope", engineScope);
		String suffix = "";
		if (isListener) {
			ctrl.listeners.put(name, cmdinfo);
			suffix = "listener";
		} else if (isService) {
			ctrl.services.put(name, cmdinfo);
			suffix = "service";
		} else if (isUtil) {
			ctrl.utils.add(fileContents.toString());
			suffix = "util";
		} else {
			ctrl.cmds.put(name, cmdinfo);
			suffix = "cmd";
		}
		log.debug("New CMD: " + name + " | Type: " + suffix);
	}

	/**
	 * Finds updated and new JS and adds to appropiate Set
	 * @param searched  TreeMap containing JS to search
	 * @param name      Filename of file
	 * @param contents  Contents of file
	 */
	private void updateChanges(TreeMap searched, String name, String contents) {
		if (searched.get(name) != null && ((TreeMap) searched.get(name)).get("src").equals(contents)) {
		} //Do nothing
		else if (searched.get(name) != null) {
			updatedCMDs.add(name);
		} else {
			newCMDs.add(name);
		}
	}
}
