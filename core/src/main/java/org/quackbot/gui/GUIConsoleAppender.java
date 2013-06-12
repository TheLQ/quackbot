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
package org.quackbot.gui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quackbot.Controller;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Appender for everything thats not bot. All events from Bot are ignored
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class GUIConsoleAppender extends AppenderBase<ILoggingEvent> {
	protected final Controller controller;
	protected final GUI gui;
	protected final PatternLayout messageLayout;
	protected final SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss a");
	protected final LinkedList<ILoggingEvent> initMessageQueue = new LinkedList<ILoggingEvent>();
	protected boolean inited = false;

	public GUIConsoleAppender(Controller controller, GUI gui) {
		Preconditions.checkNotNull(controller, "Controller cannot be null");
		Preconditions.checkNotNull(gui, "GUI cannot be null");
		this.controller = controller;
		this.gui = gui;
		
		//Grab the context from root logger
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		setContext(rootLogger.getLoggerContext());
		
		//Setup message layout
		messageLayout = new PatternLayout();
		messageLayout.setContext(getContext());
		messageLayout.setPattern("%message%n");
		messageLayout.start();
		
		//Start
		start();
		rootLogger.addAppender(this);
		log.debug("Added GUILogAppender, waiting for init");
	}

	public void init() {
		//Init styles
		for (JTextPane curPane : ImmutableList.of(gui.BerrorLog, gui.CerrorLog)) {
			StyledDocument doc = curPane.getStyledDocument();
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

		log.debug("Inited GUILogAppender, processing any saved messages");
		synchronized (initMessageQueue) {
			inited = true;
			while (!initMessageQueue.isEmpty())
				append(initMessageQueue.poll());
		}
	}

	/**
	 * Used by Log4j to write something from the LoggingEvent. This simply points to
	 * WriteOutput which writes to the the GUI or to the console
	 * @param event
	 */
	@Override
	public void append(final ILoggingEvent event) {
		if (!inited)
			synchronized (initMessageQueue) {
				if (!inited) {
					initMessageQueue.add(event);
					return;
				}
			}

		//Grab bot info off of MDC
		final String botId = MDC.get("pircbotx.id");
		final String botServer = MDC.get("pircbotx.server");
		final String botPort = MDC.get("pircbotx.port");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					//Figure out where this is going
					JTextPane textPane = (StringUtils.isBlank(botId)) ? gui.CerrorLog : gui.BerrorLog;
					JScrollPane scrollPane = (StringUtils.isBlank(botId)) ? gui.CerrorScroll : gui.BerrorScroll;

					//Configure stle
					StyledDocument doc = textPane.getStyledDocument();
					Style msgStyle = event.getLevel().isGreaterOrEqual(Level.WARN) ? doc.getStyle("Error") : doc.getStyle("Normal");

					doc.insertString(doc.getLength(), "\n", doc.getStyle("Normal"));
					int prevLength = doc.getLength();
					doc.insertString(doc.getLength(), "[" + dateFormatter.format(event.getTimeStamp()) + "] ", doc.getStyle("Normal")); //time
					//doc.insertString(doc.getLength(), "["+event.getThreadName()+"] ", doc.getStyle("Thread")); //thread name
					doc.insertString(doc.getLength(), event.getLevel().toString() + " ", doc.getStyle("Level")); //Logging level
					doc.insertString(doc.getLength(), event.getLoggerName() + " ", doc.getStyle("Class"));
					if (StringUtils.isNotBlank(botId)) {
						String port = !botPort.equals("6667") ? ":" + botPort : "";
						doc.insertString(doc.getLength(), "<" + botId + ":" + botServer + port + "> ", doc.getStyle("Server"));
					}
					doc.insertString(doc.getLength(), messageLayout.doLayout(event).trim(), msgStyle);

					//Only autoscroll if the scrollbar is at the bottom
					//JScrollBar scrollBar = scroll.getVerticalScrollBar();
					//if (scrollBar.getVisibleAmount() != scrollBar.getMaximum() && scrollBar.getValue() + scrollBar.getVisibleAmount() == scrollBar.getMaximum())
					textPane.setCaretPosition(prevLength);
				} catch (Exception e) {
					addError("Exception encountered when logging", e);
				}
			}
		});
	}
}
