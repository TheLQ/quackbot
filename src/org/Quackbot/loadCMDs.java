/**
 * @(#)loadCMDs.java
 *
 * Load (or reload) all CMD classes
 *  -Dump into controller instance
 *
 * @author Lord.Quackstar
 */

package org.Quackbot;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.net.*;
import javax.script.*;

import org.apache.commons.lang.StringUtils;

import org.Quackbot.CMDs.CMDSuper;

public class loadCMDs extends Thread {	
	Controller ctrl = null;
	TreeSet<String> newCMDs = new TreeSet<String>();
	TreeSet<String> updatedCMDs = new TreeSet<String>();
	TreeSet<String> deletedCMDs = new TreeSet<String>();
	TreeMap<String,TreeMap<String,Object>> cmdBack;
	
	public loadCMDs(Controller ctrl) {
		this.ctrl = ctrl;
	}
		
	@Override
	public void run() {
		cmdBack = (TreeMap<String,TreeMap<String,Object>>)ctrl.cmds.clone();
		try {
			ctrl.cmds.clear();
			File cmddir = new File("../CMDs");
			if(!cmddir.exists()) {
				System.out.println("CMD directory not found!");
				interrupt();
			}
			
			//Call recursive file method
			traverse(cmddir);
			
			System.out.println("cmdBack len: "+cmdBack.size()+" | cmds len: "+ctrl.cmds.size());
			
			//Get deleted CMDs
			Iterator cmdItr = cmdBack.keySet().iterator();
			while(cmdItr.hasNext()) {
				String curCmd = (String)cmdItr.next();
				if(!ctrl.cmds.containsKey(curCmd) && !newCMDs.contains(curCmd))
					deletedCMDs.add(curCmd);
			}
			
			String newStr = (newCMDs.size() == 0) ? "" : "New: "+StringUtils.join(newCMDs.toArray(),", ");
			String updateStr = (updatedCMDs.size() == 0) ? "" : "Updated: "+StringUtils.join(updatedCMDs.toArray(),", ");
			String delStr = (deletedCMDs.size() == 0) ? "" : "Deleted: "+StringUtils.join(deletedCMDs.toArray(),", ");
			
			//Notify everyone of new change
			if((newStr+updateStr+delStr).equals("")) {
				System.out.println("Reload changed nothing!");
				return;
			}
			ctrl.sendGlobalMessage("Bot commands reloaded! "+newStr+" "+updateStr+" "+delStr);
		}
		catch(Exception e) {
			System.err.println("Error in reload, reverting to cmd backup");
			ctrl.cmds = cmdBack;
			e.printStackTrace();
		}
		return;
	}
	
	public void traverse( File file ) throws Exception {
		if (file.isDirectory()) {
			final File[] childs = file.listFiles();
			for( File child : childs )
				traverse(child);
			return;
		}
		
		//Is this in the .svn directory?
		else if((file.getAbsolutePath().indexOf(".svn") != -1))
			return;
		
		//Basic setup
		ScriptEngine jsEngine = ctrl.jsEngine;
		String name = StringUtils.split(file.getName(),".")[0];
		
		//Read File Line By Line
		System.out.println("Current file: "+file.getName());
     	BufferedReader input =  new BufferedReader(new FileReader(file));
    	StringBuilder fileContents = new StringBuilder();
    	String strLine;
    	while ((strLine = input.readLine()) != null)
    		fileContents.append(strLine+System.getProperty("line.separator"));  			
    	input.close();
    	String contents = fileContents.toString();
    	
    	//Method update list: Is this an existing method?
    	if(cmdBack.get(name) != null && cmdBack.get(name).get("src").equals(contents)){
    		//Do nothing
    	}
    	else if(cmdBack.get(name) != null) {
    		System.out.println("CMD "+name+" is being updated!");
    		updatedCMDs.add(name);
    	}
    	else {
    		System.out.println("CMD "+name+" is new!");
    		newCMDs.add(name);
    	}
    	
    	//Make new context
    	ScriptContext newContext = new SimpleScriptContext();
     	Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);
     	jsEngine.eval(contents,newContext);
    	
    	//Make a treemap containing very detailed info and dump into main map
    	TreeMap<String,Object> cmdinfo = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
    	cmdinfo.put("src",fileContents.toString());
    	cmdinfo.put("help",(String)engineScope.get("help"));
		cmdinfo.put("admin",((engineScope.get("admin") == null) ? false : true));
		cmdinfo.put("ReqArg",((engineScope.get("ReqArg") == null) ? false : true));
		cmdinfo.put("param",(int)Double.parseDouble(engineScope.get("param").toString()));
		cmdinfo.put("context",newContext);
		cmdinfo.put("scope",engineScope);
    	ctrl.cmds.put(name,cmdinfo);
    	System.out.println("New CMD: "+name);
	}
}
