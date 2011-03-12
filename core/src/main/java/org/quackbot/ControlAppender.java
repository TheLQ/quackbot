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
package org.quackbot;

import org.quackbot.gui.GUI;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import java.io.PrintStream;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Appender for everything thats not bot. All events from Bot are ignored
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class ControlAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
	private PatternLayout normalGen = new PatternLayout();
	public Controller controller;

	public ControlAppender(Controller controller, LoggerContext context) {
		this.controller = controller;
		setName("ControlAppender");
		setContext(context);
		normalGen.setContext(context);
		normalGen.setPattern("%d{MM/dd/yyy hh:mm:ss a}  %-5p %c - ");
		normalGen.start();
		start();
	}

	/**
	 * Used by Log4j to write something from the LoggingEvent. This simply points to
	 * WriteOutput which writes to the the GUI or to the console
	 * @param event
	 */
	@Override
	public void append(ILoggingEvent event) {
		String server = (Bot.getPoolLocal() != null) ? Bot.getPoolLocal().getServer() : "";
		if (controller.gui != null) {
			GUI gui = controller.gui;
			JTextPane textPane = (Bot.getPoolLocal() != null) ? gui.BerrorLog : gui.CerrorLog;
			JScrollPane scrollPane = (Bot.getPoolLocal() != null) ? gui.BerrorScroll : gui.CerrorScroll;
			SwingUtilities.invokeLater(new WriteOutput(textPane, scrollPane, event, server));
		} else {
			PrintStream output = (event.getLevel().isGreaterOrEqual(Level.WARN)) ? System.err : System.out;
			if (event.getThrowableProxy() == null)
				output.println(normalGen.doLayout(event).trim() + event.getFormattedMessage());
			else
				output.println(event.getFormattedMessage() + "\n" + ExceptionUtils.getFullStackTrace(((ThrowableProxy) event.getThrowableProxy()).getThrowable()));
		}
	}

	/**0
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
		JScrollPane scroll;

		/**
		 * Simple constructor to init
		 * @param appendTo JTextPane to append to
		 */
		public WriteOutput(JTextPane appendTo, JScrollPane scroll, ILoggingEvent event, String address) {
			this.pane = appendTo;
			this.doc = appendTo.getStyledDocument();
			this.address = address;
			this.event = event;
			this.scroll = scroll;

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
				String message = event.getFormattedMessage().trim();

				//don't print empty strings
				if (StringUtils.isBlank(name.trim()))
					return;

				Style msgStyle = null;
				if (event.getLevel().isGreaterOrEqual(Level.WARN))
					msgStyle = doc.getStyle("Error");
				else if (message.startsWith("###")) {
					msgStyle = doc.getStyle("Error");
					message = message.substring(3);
				} else if (message.startsWith(">>>")) {
					msgStyle = doc.getStyle("BotSend");
					message = message.substring(3);
				} else if (message.startsWith("@@@")) {
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
				if (StringUtils.isNotBlank(address))
					doc.insertString(doc.getLength(), "<" + address + "> ", doc.getStyle("Server"));
				doc.insertString(doc.getLength(), formatMsg(event, address, message), msgStyle);

				//Only autoscroll if the scrollbar is at the bottom
				//JScrollBar scrollBar = scroll.getVerticalScrollBar();
				//if (scrollBar.getVisibleAmount() != scrollBar.getMaximum() && scrollBar.getValue() + scrollBar.getVisibleAmount() == scrollBar.getMaximum())
					pane.setCaretPosition(prevLength);
			} catch (Exception e) {
				e.printStackTrace(); //Don't use log.error because this is how stuff is outputed
			}
		}

		public String formatMsg(ILoggingEvent event, String address, String message) {
			ThrowableProxy throwArr = (ThrowableProxy) event.getThrowableProxy();
			if (throwArr == null)
				return message;
			return message + "\n" + ExceptionUtils.getFullStackTrace(((ThrowableProxy) throwArr).getThrowable()).trim();
		}
	}
}
