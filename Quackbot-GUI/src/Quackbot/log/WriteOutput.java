/**
 * @(#)ControlAppender.java
 *
 * This file is part of Quackbot
 */
package Quackbot.log;

import java.awt.Color;

import java.text.SimpleDateFormat;

import javax.swing.JTextPane;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;

import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Utility for writing to output TextFields on GUI using standard format.
 * <p>
 * This should ONLY be executed in AWT Event Queue
 * @author Lord.Quackstar
 */
public class WriteOutput implements Runnable {
	/**
	 * Pane to write to
	 */
	JTextPane pane;
	/**
	 * StyledDocument of pane
	 */
	StyledDocument doc;
	/**
	 * Date formatter, used to get same date format
	 */
	SimpleDateFormat dateFormatter;
	/**
	 * Appender that is using this
	 */
	AppenderSkeleton appender;
	LoggingEvent event;
	String address;

	public WriteOutput(JTextPane appendTo, AppenderSkeleton appender, LoggingEvent event) {
		this(appendTo, appender, event, null);
	}

	/**
	 * Simple constructor to init
	 * @param appendTo JTextPane to append to
	 */
	public WriteOutput(JTextPane appendTo, AppenderSkeleton appender, LoggingEvent event, String address) {
		this.appender = appender;
		this.pane = appendTo;
		this.doc = appendTo.getStyledDocument();
		this.dateFormatter = new SimpleDateFormat("hh:mm:ss a");
		this.address = address;
		this.event = event;

		Style style = doc.addStyle("Class", null);
		StyleConstants.setForeground(style, Color.blue);

		style = doc.addStyle("Normal", null);

		style = doc.addStyle("Error", null);
		StyleConstants.setForeground(style, Color.red);

		style = doc.addStyle("BotSend", null);
		StyleConstants.setForeground(style, Color.ORANGE);

		style = doc.addStyle("Server", null);
		StyleConstants.setBold(style, true);

		style = doc.addStyle("Thread", null);
		StyleConstants.setItalic(style, true);

		style = doc.addStyle("Level", null);
		StyleConstants.setItalic(style, true);
	}

	public void run() {
		try {
			//get string version
			String aString = event.getRenderedMessage();

			//don't print empty strings
			if (aString == null || aString.length() <= 2)
				return;

			Style msgStyle;
			String message = event.getMessage().toString();
			if (event.getLevel().isGreaterOrEqual(Level.WARN) || message.substring(0, 3).equals("###"))
				msgStyle = doc.getStyle("Error");
			else if (message.substring(0, 3).equals(">>>"))
				msgStyle = doc.getStyle("BotSend");
			else
				msgStyle = doc.getStyle("Normal");

			doc.insertString(doc.getLength(), "\n", doc.getStyle("Normal"));
			doc.insertString(doc.getLength(), "[" + dateFormatter.format(event.timeStamp) + "] ", doc.getStyle("Normal")); //time
			//doc.insertString(doc.getLength(), "["+event.getThreadName()+"] ", doc.getStyle("Thread")); //thread name
			doc.insertString(doc.getLength(), event.getLevel().toString() + " ", doc.getStyle("Level")); //Logging level
			doc.insertString(doc.getLength(), event.getLoggerName() + " ", doc.getStyle("Class"));
			if (address != null)
				doc.insertString(doc.getLength(), "<" + address + "> ", doc.getStyle("Server"));
			doc.insertString(doc.getLength(), formatMsg(event, address), msgStyle);

			pane.setCaretPosition(doc.getLength());
		} catch (Exception e) {
			e.printStackTrace(); //Don't use log.error because this is how stuff is outputed
		}
	}

	public String formatMsg(LoggingEvent event, String address) {
		String[] throwArr = event.getThrowableStrRep();
		if (throwArr == null)
			return event.getMessage().toString();
		return event.getMessage().toString() + "\n" + StringUtils.join(throwArr, " \n");
	}

	public static void writeStd(LoggingEvent event) {
		PatternLayout pl = new PatternLayout("%d{MM/dd/yyy hh:mm:ss a}  %-5p %c - %m");
		if (event.getThrowableInformation() == null)
			System.out.println(pl.format(event));
		else {
			System.out.println(pl.format(event));
			System.out.println(StringUtils.join(event.getThrowableStrRep(), " \n"));
		}
	}
}
//Log4j PatternLayout config (what this is supposed to look like) "%d{MM/dd/yyy hh:mm:ss a} | [%t] | %-5p | %c{2} | - "+extra+" %m"
