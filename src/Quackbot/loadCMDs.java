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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang.StringUtils;

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
	String curFile = "";

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
		try {
			ctrl.cmds.clear();
			ctrl.listeners.clear();
			ctrl.threadPool_js.shutdownNow();
			ctrl.threadPool_js = Executors.newCachedThreadPool();
			File cmddir = new File("js");
			if (!cmddir.exists()) {
				System.out.println("CMD directory not found! CD: " + new File(".").getAbsolutePath());
				return;
			}

			//Call recursive file method
			traverse(cmddir);

			System.out.println("cmdBack len: " + cmdBack.size() + " | cmds len: " + ctrl.cmds.size());

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
				System.out.println("Reload changed nothing!");
				return;
			}
			ctrl.sendGlobalMessage("Bot commands reloaded! " + newStr + " " + updateStr + " " + delStr);
		} catch (Exception e) {
			System.err.println("Error in reload, reverting to cmd backup");
			System.err.println("Last file: " + this.curFile);
			ctrl.cmds = cmdBack;
			e.printStackTrace();
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

		//Method update list: Is this an existing method?
		if (isListener(file)) {
			updateChanges(listenerBack, name, contents);
		} else if (isService(file)) {
			updateChanges(servicesBack, name, contents);
		} else {
			updateChanges(cmdBack, name, contents);
		}

		//Make new context
		ScriptContext newContext = new SimpleScriptContext();
		Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
		engineScope.put("ctrl", ctrl);
		jsEngine.eval(contents, newContext);

		//Make a treemap containing very detailed info and dump into main map
		TreeMap<String, Object> cmdinfo = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		cmdinfo.put("src", fileContents.toString());
		cmdinfo.put("help", (String) engineScope.get("help"));
		cmdinfo.put("admin", ((engineScope.get("admin") == null) ? false : true));
		cmdinfo.put("ReqArg", ((engineScope.get("ReqArg") == null) ? false : true));
		cmdinfo.put("param", (int) Double.parseDouble(engineScope.get("param").toString()));
		cmdinfo.put("ignore", ((engineScope.get("ignore") == null) ? false : true));
		cmdinfo.put("context", newContext);
		cmdinfo.put("scope", engineScope);
		String suffix = "";
		if (isListener(file)) {
			ctrl.listeners.put(name, cmdinfo);
			suffix = "listeners";
		} else if (isService(file)) {
			ctrl.services.put(name, cmdinfo);
			suffix = "services";
		} else {
			ctrl.cmds.put(name, cmdinfo);
			suffix = "cmd";
		}
		System.out.println("New CMD: " + name + " | Type: " + suffix);
	}

	/**
	 * Detects if file is a listener by checking if its in the listener directory
	 * @param file  The file to check
	 * @return      True if it is, false otherwise
	 */
	private boolean isListener(File file) {
		if (file.toString().indexOf("listeners") != -1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Is this a service?
	 * @param file  The file to check
	 * @return      True if it is, false otherwise
	 */
	private boolean isService(File file) {
		if (file.toString().indexOf("services") != -1) {
			return true;
		} else {
			return false;
		}
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
