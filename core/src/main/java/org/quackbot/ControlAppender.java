/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
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
package org.quackbot;

import java.util.List;
import org.quackbot.gui.GUI;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * Appender for everything thats not bot. All events from Bot are ignored
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class ControlAppender extends AppenderBase<ILoggingEvent> {
	@Setter
	protected Controller controller;
	@Getter
	@Setter
	protected PatternLayoutEncoder encoder;
	protected LinkedBlockingQueue<ILoggingEvent> guiQueue = new LinkedBlockingQueue<ILoggingEvent>();
	protected LinkedList<Bot> botQueue = new LinkedList<Bot>();
	protected WriteThread writeThread = new WriteThread();
	protected boolean useQueue = true;

	/**
	 * Used by Log4j to write something from the LoggingEvent. This simply points to
	 * WriteOutput which writes to the the GUI or to the console
	 * @param event
	 */
	@Override
	public void append(ILoggingEvent event) {
		try {
			//Push the bot and the event onto the queue, with the bot first so its not taken before its added
			if (useQueue) {
				botQueue.add(Bot.getPoolLocal());
				guiQueue.add(event);
			}
			if (controller != null)
				synchronized (writeThread) {
					//If there's a GUI and writeThread isn't started, run it
					if (controller.getGui() != null && !writeThread.isRunning())
						writeThread.execute();
					//If there's no GUI but useQueue is still true, undo everything
					if (controller.getGui() == null && useQueue) {
						useQueue = false;
						botQueue.clear();
						guiQueue.clear();
					}
				}

			//If this lined is reached, print to standard out
			PrintStream output = (event.getLevel().isGreaterOrEqual(Level.WARN)) ? System.err : System.out;
			output.print(encoder.getLayout().doLayout(event));

		} catch (Exception e) {
			addError("Exception encountered when logging", e);
		}
	}

	@Override
	public void stop() {
		//Make sure to kill the writeThread when finished here
		synchronized (writeThread) {
			if (writeThread != null)
				writeThread.cancel(true);
		}
		super.stop();
	}

	protected class WriteThread extends SwingWorker<Void, ILoggingEvent> {
		/**
		 * Date formatter, used to get same date format
		 */
		protected SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss a");
		protected PatternLayout messageLayout;
		@Getter
		@Setter
		protected boolean running = false;

		public WriteThread() {
			messageLayout = new PatternLayout();
			messageLayout.setContext(getContext());
			messageLayout.setPattern("%message");
		}

		@Override
		protected Void doInBackground() throws Exception {
			try {
				while (true)
					publish(guiQueue.take());
			} catch (InterruptedException e) {
				//Were being stopped
				return null;
			}
		}

		@Override
		protected void process(List<ILoggingEvent> chunks) {
			if(messageLayout.getContext() == null) {
				messageLayout.setContext(((Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).getLoggerContext());
				messageLayout.start();
			}
			System.out.println("MESSAGE LAYOUT CONTEXT AFTER: " + messageLayout.getContext());
			for (ILoggingEvent event : chunks)
				try {
					boolean raceCondition = botQueue.isEmpty();
					Bot bot = botQueue.poll();
					GUI gui = controller.getGui();

					//Figure out where this is going
					String address = (bot != null) ? Bot.getPoolLocal().getServer() : "";
					JTextPane textPane = (bot != null) ? gui.BerrorLog : gui.CerrorLog;
					JScrollPane scrollPane = (bot != null) ? gui.BerrorScroll : gui.CerrorScroll;

					//Get styled doc and configure if nessesary
					StyledDocument doc = textPane.getStyledDocument();
					if (doc.getStyle("Normal") == null) {
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

					//Warn user about a possible race condition
					if (raceCondition)
						doc.insertString(doc.getLength(), "\nERROR: Race condition detected in ControlAppender when getting next Bot. Expected botQueue to have a bot to get, but is 0", doc.getStyle("Error"));

					//get string version
					String message = event.getFormattedMessage().trim();

					Style msgStyle = null;
					if (event.getLevel().isGreaterOrEqual(Level.WARN))
						msgStyle = doc.getStyle("Error");
					else
						msgStyle = doc.getStyle("Normal");

					doc.insertString(doc.getLength(), "\n", doc.getStyle("Normal"));
					int prevLength = doc.getLength();
					doc.insertString(doc.getLength(), "[" + dateFormatter.format(event.getTimeStamp()) + "] ", doc.getStyle("Normal")); //time
					//doc.insertString(doc.getLength(), "["+event.getThreadName()+"] ", doc.getStyle("Thread")); //thread name
					doc.insertString(doc.getLength(), event.getLevel().toString() + " ", doc.getStyle("Level")); //Logging level
					doc.insertString(doc.getLength(), event.getLoggerName() + " ", doc.getStyle("Class"));
					if (StringUtils.isNotBlank(address))
						doc.insertString(doc.getLength(), "<" + address + "> ", doc.getStyle("Server"));
					doc.insertString(doc.getLength(), messageLayout.doLayout(event), msgStyle);

					//Only autoscroll if the scrollbar is at the bottom
					//JScrollBar scrollBar = scroll.getVerticalScrollBar();
					//if (scrollBar.getVisibleAmount() != scrollBar.getMaximum() && scrollBar.getValue() + scrollBar.getVisibleAmount() == scrollBar.getMaximum())
					textPane.setCaretPosition(prevLength);
				} catch (Exception e) {
					e.printStackTrace(); //Don't use log.error because this is how stuff is outputed
				}
		}
	}
}
