/**
 * @(#)GUI.java
 *
 * Provides a GUI for bot
 *  -Output is pretified
 *  -Can initate stop, start, and reload from here
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

import org.Quackbot.*;
import org.Quackbot.Annotations.*;
import org.Quackbot.CMDs.CMDSuper;

public class GUI extends JFrame implements ActionListener {
	
	JTextPane errorLog;
	JScrollPane errorScroll;
	StyledDocument errorDoc;
	PrintStream oldOut,oldErr;
	
	public Controller ctrl = null;
	
    public GUI() {
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
		
		ctrl = new Controller();
    }
    
    public void actionPerformed(ActionEvent e) {
    	String cmd = e.getActionCommand();
    	
    	if(cmd.equals("Stop")) {
    		ctrl.stopAll();
    	}
    	else if(cmd.equals("Start")) {
    		ctrl = new Controller();
    	}
    	else if(cmd.equals("Reload")) {
    		new loadCMDs(ctrl).execute();
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
	        	else if(endString[1].substring(0,3).equals("###")) style = errorDoc.getStyle("Error");
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
                new GUI();
            }
        });
    }
}