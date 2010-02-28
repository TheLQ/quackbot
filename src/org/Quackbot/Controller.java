/**
 * @(#)Controller.java
 *
 * Main Controller for bot:
 *  -Initiates and keeps track of all bots
 *  -Loads (and reload) all CMD classes
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

import org.Quackbot.*;
import org.Quackbot.CMDs.CMDSuper;

import org.apache.commons.jci.*;
import org.apache.commons.jci.compilers.*;
import org.apache.commons.jci.readers.*;
import org.apache.commons.jci.stores.*;

public class Controller extends GUI{
	
	public TreeMap<String,CMDSuper> cmds;
	public TreeMap<String,Method> methodList;
	public TreeSet<Bot> bots = new TreeSet<Bot>();
	
    public Controller() {
    	//Lets now get all CMD classes and put into array
		cmds = new TreeMap<String,CMDSuper>();
		methodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);

		//Load current CMD classes
		botCMDLoad loader = new botCMDLoad();
		loader.execute();
		
		//Join some servers
		new botThread("irc.freenode.net",new String[]{"##newyearcountdown"});
    }
    
    //Makes all bots quit servers
    public void stopAll() {
    	//Run this inside of a seperate thread due to time to excecute
    	new SwingWorker<Void, String>() {
    		@Override
        	public Void doInBackground() {
        		Iterator botItr = bots.iterator();
		    	while(botItr.hasNext()) {
		    		Bot curBot = (Bot)botItr.next();
		    		curBot.quitServer("Killed by control panel");
		    		bots.remove(curBot);
		    	}
        	}
    	};
    }
    
    /*****Simple thread to run the bot in to prevent it from locking the gui***/
    class botThread extends SwingWorker<Void, String> {
    	String server = null;
    	String[] channels = null;
    	
    	public botThread(String server, String[] channels) {
    		this.server = server;
    		this.channels = channels;
    	}
    	      
    	@Override
        public Void doInBackground() {
        	try {
    			System.out.println("Initiating connection");
    			Bot qb = new Bot(Controller.this);
		        qb.setVerbose(true);
		        qb.connect(server);
		        qb.joinChannel("##newyearcountdown");
		        bots.add(qb);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			return null;
        }
    }
    
    /*****Simple thread to run the bot in to prevent it from locking the gui***/
    class botCMDLoad extends SwingWorker<Void, String> {
    	Boolean recomp = false;
    	
    	@Override
        public Void doInBackground() {
	    	try {
	    		File cmddir = new File("./org/Quackbot/CMDs");
			    ReloadingClassLoader classloader = new ReloadingClassLoader(Controller.this.getClass().getClassLoader());
		        if (!cmddir.exists()) {
		            System.out.println("Directory "+cmddir.toString()+" does not exist!");
		            cancel(true);
		        }

	         	File[] files = cmddir.listFiles();
		        for (File file : files) {
		        	String name = file.getName();
		        	String className = name.split("\\.")[0];
		        	if(name.equals(".svn") || className.equals("CMDSuper")) continue;
		        	System.out.println("Java file found! Filename: "+name+" ClassName: "+className);
		        	
		        	//Recompile?
		       		if(recomp) {
		        		System.out.println("Compiling class "+className);
				     	JavaCompiler compiler = new JavaCompilerFactory().createCompiler("javac");
						CompilationResult result = compiler.compile(new String[]{file.toString()}, new FileResourceReader(new File("../src/org/Quackbot/CMDs")), new FileResourceStore(file.getParentFile()),Controller.this.getClass().getClassLoader());
						System.out.println( result.getErrors().length + " errors");
						System.out.println( result.getWarnings().length + " warnings");
		        	}
		        	
		        	//Load class
		        	CMDSuper classInst = (CMDSuper)classloader.loadClass("org.Quackbot.CMDs."+className).getConstructors()[0].newInstance(qb);
		        	
				    //Add all methods to class list
				    for(Method method : classInst.getClass().getDeclaredMethods()) {
				    	int modifier = method.getModifiers();
				    	String methodName = method.getName();
				    	if(modifier != Modifier.PRIVATE && modifier != Modifier.PROTECTED) {
				    		methodList.put(methodName,method);
				    		System.out.println("Name: "+methodName);
				    	}
				    }
		       		
		       		//Add instance to class list
		       		cmds.put(className,classInst);
		        }
	        }
			catch(Exception e) {
				e.printStackTrace();
			}
		return null;
       }
   }
}