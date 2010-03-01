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

import org.apache.commons.jci.*;
import org.apache.commons.jci.compilers.*;
import org.apache.commons.jci.readers.*;
import org.apache.commons.jci.stores.*;

import org.Quackbot.CMDs.CMDSuper;

public class loadCMDs extends SwingWorker<Void, String> { 	
	Controller ctrl = null;
	
	public loadCMDs(Controller ctrl) {
		this.ctrl = ctrl;
 	}
 	
 	@Override
    public Void doInBackground() {
	 	try {
	 		ctrl.methodList.clear();
	 		ctrl.cmds.clear();
	 		File cmddir = new File("./org/Quackbot/CMDs");
			    ReloadingClassLoader classloader = new ReloadingClassLoader(ctrl.getClass().getClassLoader());
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
			     	
			     	//Load class
			     	CMDSuper classInst = (CMDSuper)classloader.loadClass("org.Quackbot.CMDs."+className).newInstance();
			     	
				    //Add all methods to class list
				    for(Method method : classInst.getClass().getDeclaredMethods()) {
					 	int modifier = method.getModifiers();
					 	String methodName = method.getName();
					 	if(modifier != Modifier.PRIVATE && modifier != Modifier.PROTECTED) {
					 		ctrl.methodList.put(methodName,method);
					 		System.out.println("Name: "+methodName);
					 	}
			    	}
				    		
			    	//Add instance to class list
				    ctrl.cmds.put(className,classInst);
			    }
		    }
			catch(Exception e) {
				e.printStackTrace();
			}
			
			return null;
       }
}