/**
 * @(#)ControlAppender.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.log;

import Quackbot.Bot;
import Quackbot.GUI;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.lang.StringUtils;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

import org.apache.log4j.spi.LoggingEvent;

/**
 * Appender for everything thats not bot. All events from Bot are ignored
 * 
 * @author Lord.Quackstar
 */
public class ControlAppender extends AppenderSkeleton {
	public ControlAppender() {
		setName("ControlAppender");
	}

	/**
	 * Used by Log4j to write something from the LoggingEvent. This simply points to
	 * WriteOutput which writes to the the GUI or to the console
	 * @param event
	 */
	@Override
	public void append(LoggingEvent event) {
		if (!Bot.threadLocal.get().equals("EMPTY")) {
			if (GUIExists())
				SwingUtilities.invokeLater(new WriteOutput(GUI.instance.BerrorLog, this, event, Bot.threadLocal.get()));
			else
				writeStd(event);
			return;
		}


		if (GUIExists())
			SwingUtilities.invokeLater(new WriteOutput(GUI.instance.CerrorLog, this, event));
		else
			writeStd(event);
	}

	/**
	 * Used by Log4j to determine if this requires a layout. Since all the dirty work is
	 * done by {@link WriteOutput}, this returns false;
	 * @return False, since this is a custom layout
	 */
	@Override
	public boolean requiresLayout() {
		return false;
	}

	/**
	 * Used by Log4j to close anything that this Appender needs to close. Since this just
	 * writes to a JTextPane, this just does nothing
	 */
	@Override
	public void close() {
		//nothing to close
	}

	public boolean GUIExists() {
		return (GUI.instance != null);
	}

	public void writeStd(LoggingEvent event) {
		PatternLayout pl = new PatternLayout("%d{MM/dd/yyy hh:mm:ss a}  %-5p %c - %m");
		if (event.getThrowableInformation() == null)
			System.out.println(pl.format(event));
		else {
			System.out.println(pl.format(event));
			System.out.println(StringUtils.join(event.getThrowableStrRep(), " \n"));
		}
	}

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
		SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss a");
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
			this.address = address;
			this.event = event;

			//Only add styles if they don't already exist
			if (doc.getStyle("Class") == null) {
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
		}

		/**
		 * Actually formats and add's the text to JTextPane in AWT Event Queue
		 */
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
				int prevLength = doc.getLength();
				doc.insertString(doc.getLength(), "[" + dateFormatter.format(event.timeStamp) + "] ", doc.getStyle("Normal")); //time
				//doc.insertString(doc.getLength(), "["+event.getThreadName()+"] ", doc.getStyle("Thread")); //thread name
				doc.insertString(doc.getLength(), event.getLevel().toString() + " ", doc.getStyle("Level")); //Logging level
				doc.insertString(doc.getLength(), event.getLoggerName() + " ", doc.getStyle("Class"));
				if (address != null)
					doc.insertString(doc.getLength(), "<" + address + "> ", doc.getStyle("Server"));
				doc.insertString(doc.getLength(), formatMsg(event, address), msgStyle);

				pane.setCaretPosition(prevLength);
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
	}
}
