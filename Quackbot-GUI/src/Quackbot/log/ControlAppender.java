/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.log;

import Quackbot.Bot;
import Quackbot.GUI;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.lang.StringUtils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Appender for everything thats not bot. All events from Bot are ignored
 *
 * @author Lord.Quackstar
 */
public class ControlAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
	PatternLayout exceptionGen = new PatternLayout();
	PatternLayout normalGen = new PatternLayout();


	public ControlAppender() {
		setName("ControlAppender");
		exceptionGen.setPattern("%d{MM/dd/yyy hh:mm:ss a}  %-5p %c - %ex{full}");
		normalGen.setPattern("%d{MM/dd/yyy hh:mm:ss a}  %-5p %c - %m");
	}

	/**
	 * Used by Log4j to write something from the LoggingEvent. This simply points to
	 * WriteOutput which writes to the the GUI or to the console
	 * @param event
	 */
	@Override
	public void append(ILoggingEvent event) {
		//System.out.println("New job: " + event);
		if (Bot.getPoolLocal() != null)
			if (GUIExists()) {
				//System.out.println("Submitting to Swing with server");
				SwingUtilities.invokeLater(new WriteOutput(GUI.instance.BerrorLog, event, Bot.getPoolLocal().getServer()));
			} else {
				//System.out.println("writingstd with server");
				writeStd(event);
			}
		else if (GUIExists()) {
			//System.out.println("Submitting to Swings");
			SwingUtilities.invokeLater(new WriteOutput(GUI.instance.CerrorLog, event));
		} else {
			//System.out.println("writingstd");
			writeStd(event);
		}

	}

	public boolean GUIExists() {
		return (GUI.instance != null);
	}

	public void writeStd(ILoggingEvent event) {
		if (event.getThrowableProxy() == null)
			System.out.println(normalGen.doLayout(event));
		else
			System.out.println(exceptionGen.doLayout(event));
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
		ILoggingEvent event;
		String address;

		public WriteOutput(JTextPane appendTo, ILoggingEvent event) {
			this(appendTo, event, null);
		}

		/**
		 * Simple constructor to init
		 * @param appendTo JTextPane to append to
		 */
		public WriteOutput(JTextPane appendTo, ILoggingEvent event, String address) {
			this.pane = appendTo;
			this.doc = appendTo.getStyledDocument();
			this.address = address;
			this.event = event;

			//Only add styles if they don't already exist
			if (doc.getStyle("Class") == null) {
				doc.addStyle("Normal", null);
				StyleConstants.setForeground(doc.addStyle("Class", null), Color.blue);
				StyleConstants.setForeground(doc.addStyle("Error", null), Color.red);
				//BotSend gets a better shade of orange than Color.organ gives
				StyleConstants.setForeground(doc.addStyle("BotSend", null), new Color(255, 127, 0));
				//BotRecv gets a better shade of green than Color.green gives
				StyleConstants.setForeground(doc.addStyle("BotRecv", null), new Color(0, 159, 107));
				StyleConstants.setBold(doc.addStyle("Server", null), true);
				StyleConstants.setItalic(doc.addStyle("Thread", null), true);
				StyleConstants.setItalic(doc.addStyle("Level", null), true);
			}
		}

		/**
		 * Actually formats and add's the text to JTextPane in AWT Event Queue
		 */
		@Override
		public void run() {
			try {
				//get string version
				String aString = event.getMessage();

				//don't print empty strings
				if (aString == null || aString.length() <= 2)
					return;

				Style msgStyle = null;
				String message = event.getMessage().toString();
				if (event.getLevel().isGreaterOrEqual(Level.WARN))
					msgStyle = doc.getStyle("Error");
				else if (message.substring(0, 3).equals("###")) {
					msgStyle = doc.getStyle("Error");
					message = message.substring(3);
				} else if (message.substring(0, 3).equals(">>>")) {
					msgStyle = doc.getStyle("BotSend");
					message = message.substring(3);
				} else if (message.substring(0, 3).equals("@@@")) {
					msgStyle = doc.getStyle("BotRecv");
					message = message.substring(3);
				} else
					msgStyle = doc.getStyle("Normal");

				doc.insertString(doc.getLength(), "\n", doc.getStyle("Normal"));
				int prevLength = doc.getLength();
				doc.insertString(doc.getLength(), "[" + dateFormatter.format(event.getTimeStamp()) + "] ", doc.getStyle("Normal")); //time
				//doc.insertString(doc.getLength(), "["+event.getThreadName()+"] ", doc.getStyle("Thread")); //thread name
				doc.insertString(doc.getLength(), event.getLevel().toString() + " ", doc.getStyle("Level")); //Logging level
				doc.insertString(doc.getLength(), event.getLoggerName() + " ", doc.getStyle("Class"));
				if (address != null)
					doc.insertString(doc.getLength(), "<" + address + "> ", doc.getStyle("Server"));
				doc.insertString(doc.getLength(), formatMsg(event, address, message), msgStyle);

				pane.setCaretPosition(prevLength);
			} catch (Exception e) {
				e.printStackTrace(); //Don't use log.error because this is how stuff is outputed
			}
		}

		public String formatMsg(ILoggingEvent event, String address, String message) {
			IThrowableProxy throwArr = event.getThrowableProxy();
			if (throwArr == null)
				return message;
			StringBuilder builder = new StringBuilder(message);

			for (StackTraceElementProxy curElem : throwArr.getStackTraceElementProxyArray())
				builder.append(curElem.toString());
			return builder.toString();
			//return message.toString() + "\n" + StringUtils.join(throwArr, " \n");
		}
	}
}
