/**
 * @(#)loadCMDs.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.JSCmdInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *  Load (or reload) all CMD classes
 *  -Dump into controller instance
 * @author Lord.Quackstar
 */
public class loadCMDs implements Runnable {

	/**
	 * Current Controller Instance
	 */
	Controller ctrl = InstanceTracker.getCtrlInst();
	/**
	 * List of new CMDs
	 */
	private TreeSet<String> newCMDs = new TreeSet<String>();
	/**
	 * List of updated CMDs
	 */
	private TreeSet<String> updatedCMDs = new TreeSet<String>();
	/**
	 * List of deleted CMDs
	 */
	private TreeSet<String> deletedCMDs = new TreeSet<String>();
	/**
	 * Controller CMDs backup, used for recovery in case of error
	 */
	private TreeMap<String, JSCmdInfo> cmdBack;
	/**
	 * List of all JS utils
	 */
	public TreeSet<String> JSUtils = new TreeSet<String>();
	/**
	 * Current file being parsed, used for reporting of errors
	 */
	private String curFile = "";
	/**
	 * Log4j logger
	 */
	private Logger log = Logger.getLogger(loadCMDs.class);

	/**
	 * Initate recursive scan in seperate thread. Reports to bots any updates
	 */
	public void run() {
		cmdBack = new TreeMap<String, JSCmdInfo>(ctrl.JSplugins);
		try {
			ctrl.JSplugins.clear();
			ctrl.threadPool_js.shutdownNow();
			ctrl.threadPool_js = Executors.newCachedThreadPool();
			File cmddir = new File("plugins");
			if (!cmddir.exists()) {
				log.fatal("CMD directory not found! CD: " + new File(".").getAbsolutePath());
				return;
			}

			//Call recursive file method, will parse and load all cmds
			traverse(cmddir);

			//Get one big combined string of util stuff
			StringBuilder utilSB = new StringBuilder();
			for(String curUtil : JSUtils)
				utilSB.append(curUtil);

			//Add Utils to ALL commands
			Set<Map.Entry<String,JSCmdInfo>> cmdSet = ctrl.JSplugins.entrySet();
			for(Map.Entry<String,JSCmdInfo> curCmd : cmdSet)
				new ScriptEngineManager().getEngineByName("JavaScript").eval(utilSB.toString(), curCmd.getValue().getContext());

			log.debug("cmdBack len: " + cmdBack.size() + " | cmds len: " + ctrl.JSplugins.size());

			//Get deleted CMDs
			Iterator cmdItr = cmdBack.keySet().iterator();
			while (cmdItr.hasNext()) {
				String curCmd = (String) cmdItr.next();
				if (!ctrl.JSplugins.containsKey(curCmd) && !newCMDs.contains(curCmd))
					deletedCMDs.add(curCmd);
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
			Set<Map.Entry<String, JSCmdInfo>> servItr = ctrl.JSplugins.entrySet();
			for (Map.Entry<String, JSCmdInfo> curServ : servItr) {
				JSCmdInfo cmdInfo = curServ.getValue();
				if (!cmdInfo.isService())
					continue;
				log.debug("Excecuting service");
				ctrl.threadPool_js.execute(new PluginExecutor(cmdInfo.getName(), new String[0]));
			}
		} catch (Exception e) {
			ctrl.JSplugins = cmdBack;
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
			for (File child : childs)
				traverse(child);
			return;
		} //Is this in the .svn directory?
		else if (file.getAbsolutePath().indexOf(".svn") != -1 || file.getName().equals("JS_Template.js"))
			return;

		//Basic setup
		String name = StringUtils.split(file.getName(), ".")[0];
		this.curFile = name;

		//Read File Line By Line
		BufferedReader input = new BufferedReader(new FileReader(file));
		StringBuilder fileContents = new StringBuilder();
		String strLine;
		while ((strLine = input.readLine()) != null)
			fileContents.append(strLine + System.getProperty("line.separator"));
		input.close();
		String contents = fileContents.toString();

		//Make new context
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		ScriptContext newContext = new SimpleScriptContext();
		Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
		engineScope.put("ctrl", ctrl);
		jsEngine.eval(contents, newContext);

		//Fill in cmd Info
		JSCmdInfo cmdInfo = new JSCmdInfo();
		cmdInfo.setName(name);
		cmdInfo.setAdmin((engineScope.get("admin") == null) ? false : true);
		cmdInfo.setService((engineScope.get("service") == null) ? false : true);
		cmdInfo.setListener((engineScope.get("listener") == null) ? false : true);
		cmdInfo.setIgnore((engineScope.get("ignore") == null) ? false : true);
		cmdInfo.setUtil((engineScope.get("util") == null) ? false : true);
		cmdInfo.setSrc(fileContents.toString());
		cmdInfo.setHelp((String) engineScope.get("help"));
		cmdInfo.setReqArg(((engineScope.get("ReqArg") == null) ? false : true));
		if (!cmdInfo.isListener() && !cmdInfo.isService() && !cmdInfo.isUtil())
			cmdInfo.setParams((int) Double.parseDouble(engineScope.get("param").toString()));
		cmdInfo.setContext(newContext);
		cmdInfo.setScope(engineScope);


		//Method update list: Is this an existing method?
		String suffix = "";
		if (cmdInfo.isUtil()) {
			//Customized search due to theis being a TreeSet
			JSUtils.add(fileContents.toString());
			suffix = "util";
		} else {
			ctrl.JSplugins.put(name, cmdInfo);
			suffix = "cmd";
			updateChanges(cmdBack, name, contents);
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
		else if (searched.get(name) != null)
			updatedCMDs.add(name);
		else
			newCMDs.add(name);
	}
}
