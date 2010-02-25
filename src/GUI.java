/**
 * @(#)GUI.java
 *
 *
 * @author 
 * @version 1.00 2010/2/16
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

public class GUI extends JFrame implements ActionListener {
	
	JTextPane errorLog;
	JScrollPane errorScroll;
	StyledDocument errorDoc;
	PrintStream oldOut,oldErr;
	Quackbot qb = new Quackbot();
	
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
    	
      	oldOut = System.out;
      	oldErr = System.err;
		System.setOut(new PrintStream(new FilteredStream(new ByteArrayOutputStream(),false)));
		System.setErr(new PrintStream(new FilteredStream(new ByteArrayOutputStream(),true)));
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Will exit when close button is pressed
     	setTitle("Quackbot Panel");
       	setMinimumSize(new Dimension(1000,700));
       	
       	
       	JPanel contentPane = new JPanel();
       	contentPane.setLayout(new BorderLayout());
       	contentPane.add(errorScroll,BorderLayout.CENTER);
       	
       	JPanel bottom = new JPanel();
       	JButton cancel = new JButton("Stop");
       	cancel.addActionListener(this);
       	bottom.add(cancel,BorderLayout.SOUTH);
       	JButton start = new JButton("Start");
       	start.addActionListener(this);
       	bottom.add(start,BorderLayout.SOUTH);
       	
       	contentPane.add(bottom,BorderLayout.SOUTH);
       	
       	add(contentPane); //add to JFrame
		setVisible(true); //make JFrame visible
    }
    
    public void actionPerformed(ActionEvent e) {
    	String cmd = e.getActionCommand();
    	
    	if(cmd.equals("Stop")) {
    		qb.quitServer("Forced die by server");
    	}
    	else if(cmd.equals("Start")) {
    		botThread thread = new botThread();
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
	        		
	        	Style style = null;
	        	if(error) style = errorDoc.getStyle("Error");
	        	else if(callingClass.equals("Quackbot")) style = errorDoc.getStyle("BotTalk");
	        	else style = errorDoc.getStyle("Time");
	            
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
            	System.out.println("Starting!");
                try {
                	new GUI();
                }
                catch(Exception e) {
                	e.printStackTrace();
                }
            }
        });
    }
}