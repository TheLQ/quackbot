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
import java.security.MessageDigest;

import org.Quackbot.CMDs.CMDSuper;

public class loadCMDs extends Thread {	
	Controller ctrl = null;
	
	public loadCMDs(Controller ctrl) {
		this.ctrl = ctrl;
	}
		
	@Override
	public void run() {
		try {
			ctrl.cmds.clear();
			File cmddir = new File("../CMDs");
			if(!cmddir.exists()) {
				System.out.println("CMD directory not found!");
				interrupt();
			}
			
			//Call recursive file method
			traverse(cmddir);
		}
		catch(Exception e) {
			System.out.println("Error");
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
		
		ScriptEngine jsEngine = ctrl.jsEngine;
		MessageDigest digest = MessageDigest.getInstance("MD5");
		
		//Read File Line By Line
		System.out.println("Current file: "+file.getName());
     	BufferedReader input =  new BufferedReader(new FileReader(file));
    	StringBuilder fileContents = new StringBuilder();
    	String strLine;
    	while ((strLine = input.readLine()) != null)
    		fileContents.append(strLine+System.getProperty("line.separator"));  			
    	input.close();
    	String contents = fileContents.toString();
    	
    	//Get CMD file hash
    	digest.reset();
    	digest.update(contents.getBytes());
    	String hash = new String(digest.digest());
    		
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
		String name = file.getName().split("\\.")[0];
    	ctrl.cmds.put(name,cmdinfo);
    	System.out.println("New CMD: "+name);
	}
}
