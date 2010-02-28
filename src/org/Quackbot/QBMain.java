/**
 * @(#)GUI.java
 *
 *
 * @author 
 * @version 1.00 2010/2/16
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

import org.Quackbot.*;
import org.Quackbot.Annotations.*;
import org.Quackbot.CMDs.CMDSuper;

public class QBMain extends JFrame implements ActionListener {
	
	JTextPane errorLog;
	JScrollPane errorScroll;
	StyledDocument errorDoc;
	PrintStream oldOut,oldErr;
	
	//Static class refrences
	public TreeMap<String,CMDSuper> cmds;
	public TreeMap<String,Method> methodList;
	public Bot qb = new Bot(this);
	
    public QBMain() {
    	/***Pre init, setup error log**/
    	errorLog = new JTextPane();
		errorLog.setEditable(false);
		errorLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		errorDoc = errorLog.getStyledDocument();
		errorScroll = new JScrollPane(errorLog);
		//Globs.setSize(errorScroll,125,0);
		errorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		errorScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
		TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
    	
      	oldOut = System.out;
      	oldErr = System.err;
		System.setOut(new PrintStream(new FilteredStream(new ByteArrayOutputStream(),false)));
		System.setErr(new PrintStream(new FilteredStream(new ByteArrayOutputStream(),true)));
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Will exit when close button is pressed
     	setTitle("Quackbot GUI Control Panel");
       	setMinimumSize(new Dimension(1000,700));
       	
       	JPanel contentPane = new JPanel();
       	contentPane.setLayout(new BorderLayout());
       	contentPane.add(errorScroll,BorderLayout.CENTER);
       	
       	JPanel bottom = new JPanel();
       	JButton cancel = new JButton("Stop");
       	cancel.addActionListener(this);
       	bottom.add(cancel);
       	JButton start = new JButton("Start");
       	start.addActionListener(this);
       	bottom.add(start);
       	JButton reload = new JButton("Reload");
       	reload.addActionListener(this);
       	bottom.add(reload);
       	
       	contentPane.add(bottom,BorderLayout.SOUTH);
       	
       	add(contentPane); //add to JFrame
		setVisible(true); //make JFrame visible
		
		//Lets now get all CMD classes and put into array
		
		cmds = new TreeMap<String,CMDSuper>();
		methodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);

		//Load current CMD classes
		botCMDLoad loader = new botCMDLoad();
		loader.execute();

		
		botThread thread = new botThread();
    	//thread.execute();
    }
    
    public void actionPerformed(ActionEvent e) {
    	String cmd = e.getActionCommand();
    	
    	if(cmd.equals("Stop")) {
    		qb.quitServer("Killed by control panel");
    		System.exit(0);
    	}
    	else if(cmd.equals("Start")) {
    		botThread thread = new botThread();
    		thread.execute();
    	}
    	else if(cmd.equals("Reload")) {
    		botCMDLoad loader = new botCMDLoad();
			loader.recomp = true;
			loader.execute();
    	}
    }
    
    /*****Simple thread to run the bot in to prevent it from locking the gui***/
    class botThread extends SwingWorker<Void, String> {      
    	@Override
        public Void doInBackground() {
        	try {
    			System.out.println("Initiating connection");
		        qb.setVerbose(true);
		        qb.connect("irc.freenode.net");
		        qb.joinChannel("##newyearcountdown");
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
			    ReloadingClassLoader classloader = new ReloadingClassLoader(QBMain.this.getClass().getClassLoader());
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
						CompilationResult result = compiler.compile(new String[]{file.toString()}, new FileResourceReader(new File("../src/org/Quackbot/CMDs")), new FileResourceStore(file.getParentFile()),QBMain.this.getClass().getClassLoader());
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
    
    /***Output Wrapper, Redirects all ouput to log at bottom***/
    class FilteredStream extends FilterOutputStream {
    	AttributeSet className, text;
    	boolean error;
    	
        public FilteredStream(OutputStream aStream,boolean error) {
            super(aStream);
            this.error = error;
            
          	Style style = errorLog.addStyle("Class", null); 
          	StyleConstants.setForeground(style, Color.ORANGE ); 
          	
          	style = errorLog.addStyle("Time", null); 
          	StyleConstants.setForeground(style, Color.BLUE ); 
          	        	
          	style = errorLog.addStyle("Normal", null); 
          	
          	style = errorLog.addStyle("Error", null); 
          	StyleConstants.setForeground(style, Color.red); 	
          		
          	style = errorLog.addStyle("BotTalk", null); 
          	StyleConstants.setForeground(style, Color.GREEN); 
          		
          	style = errorLog.addStyle("BotSend", null); 
          	StyleConstants.setForeground(style, Color.ORANGE); 	
       	}

        public void write(byte b[], int off, int len) throws IOException {
        	try {
	            //get string version
	            String aString = new String(b , off , len).trim();
	            
	            //don't print empty strings
	            if(aString.length()==0)
	            	return;
	        	
	        	//get calling class name
	        	StackTraceElement[] elem = Thread.currentThread().getStackTrace();
	        	String callingClass = elem[10].getClassName();
	        	
	        	//Capture real class from error message
	        	if(callingClass.equals("java.lang.Throwable"))
	        		callingClass = elem[12].getClassName();
	        	
	            //Break apart string
	            String[] endString = new String[2];
	            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
				formatter.setLenient(false);
	            try {
	            	String[] sString = aString.split(" ",2);
					endString[0] = formatter.format(new Date(Long.parseLong(sString[0])));
					endString[1] = sString[1];
	            }
	            catch(NumberFormatException e) {
	            	endString[0] = formatter.format(new Date());
	            	endString[1] = aString;
	            }
	            
	           	//Set style
	        	Style style = null;
	        	if(error) style = errorDoc.getStyle("Error");
	        	else if(callingClass.equals("Bot")) style = errorDoc.getStyle("BotTalk");
	        	else if(endString[1].substring(0,3).equals(">>>")) style = errorDoc.getStyle("BotSend");
	        	else style = errorDoc.getStyle("Time");
	            
	            if(errorDoc.getLength()!=0)
	        		errorDoc.insertString(errorDoc.getLength(),"\n",errorDoc.getStyle("Normal"));
		        errorDoc.insertString(errorDoc.getLength(),endString[0]+": ",style);
		        //errorDoc.insertString(errorDoc.getLength(),callingClass+" - ",errorDoc.getStyle("Class"));
		        errorDoc.insertString(errorDoc.getLength(),endString[1],errorDoc.getStyle("Normal"));

	        	if(error) 
	        		oldErr.println(aString); //so runtime errors can be caught
	        	else
	        		oldOut.println(aString); //so runtime errors can be caught
	        	
	        	errorLog.repaint();
	        	errorLog.revalidate();
				errorLog.setCaretPosition(errorDoc.getLength()); 
        	}
			catch (BadLocationException ble) {
            	oldErr.println("Error");
        	}
   			catch(Exception e) {
				e.printStackTrace(oldErr);
			}
        }
    }
    
    public static void main(String[] args) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new QBMain();
            }
        });
    }
}