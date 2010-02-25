/**
 * @(#)GUI.java
 *
 *
 * @author 
 * @version 1.00 2010/2/16
 */

package Quackbot;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.io.*;

import org.apache.commons.jci.*;
import org.apache.commons.jci.compilers.*;
import org.apache.commons.jci.readers.*;
import org.apache.commons.jci.stores.*;

import Quackbot.*;
import Quackbot.CMDs.*;

public class QBMain extends JFrame implements ActionListener {
	
	JTextPane errorLog;
	JScrollPane errorScroll;
	StyledDocument errorDoc;
	PrintStream oldOut,oldErr;
	
	//Static class refrences
	public static Quackbot qb = new Quackbot();
	public static AdminOnly AdminOnly = new AdminOnly(qb);
	public static GeneralUser GeneralUser = new GeneralUser(qb);
	
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
		
		
		botThread thread = new botThread();
    	thread.execute();
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
    		botRecomp thread = new botRecomp();
    		thread.execute();
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
    class botRecomp extends SwingWorker<Void, String> {      
    	@Override
        public Void doInBackground() {
        	try {
    			System.out.println("RELOADING ALL CLASSES");
    			JavaCompiler compiler = new JavaCompilerFactory().createCompiler("javac");
    			System.out.println("hehehe");
				CompilationResult result = compiler.compile(new String[]{"AdminOnly.java"}, new FileResourceReader(new File("CMDs")),new FileResourceStore(new File("CMDs")));
				System.out.println("hehehe");
				System.out.println( result.getErrors().length + " errors");
				System.out.println("hehehe");
				System.out.println( result.getWarnings().length + " warnings");
				System.out.println("hehehe");
				ReloadingClassLoader classloader = new ReloadingClassLoader(this.getClass().getClassLoader());
				System.out.println("hehehe");
				AdminOnly = (AdminOnly)classloader.loadClass("AdminOnly").newInstance();
				System.out.println("hehehe");
			}
			catch(Exception ex) {
				ex.printStackTrace();
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
	        	else if(callingClass.equals("Quackbot")) style = errorDoc.getStyle("BotTalk");
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