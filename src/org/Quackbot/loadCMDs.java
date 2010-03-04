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
		System.out.println("methodList length: "+ctrl.methodList.size()+" | cmds length: "+ctrl.cmds.size());
		File cmddir = new File("../CMDs");
		if(!cmddir.exists()) {
			System.out.println("CMD directory not found!");
			cancel(true);
		}
		
		//Using JS engine
		
        // create JavaScript engine
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        // evaluate JavaScript code from given file - specified by first argument
        engine.eval(new FileReader(file));
    }
}


		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
