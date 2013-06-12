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
import ch.qos.logback.classic.PatternLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quackbot.Controller;
import org.slf4j.MDC;

/**
 * Appender for everything thats not bot. All events from Bot are ignored
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class GUIConsoleAppender extends AppenderBase<ILoggingEvent> {
	protected final Controller controller;
	protected final PatternLayout messageLayout;
	protected final SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss a");

	public GUIConsoleAppender(Controller controller) {
		this.controller = controller;
		messageLayout = new PatternLayout();
		messageLayout.setContext(getContext());
		messageLayout.setPattern("%message%n");
	}

	/**
	 * Used by Log4j to write something from the LoggingEvent. This simply points to
	 * WriteOutput which writes to the the GUI or to the console
	 * @param event
	 */
	@Override
	public void append(final ILoggingEvent event) {

		final GUI gui = controller.getGui();
		if (gui == null)
			//Can't log anything
			return;

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
