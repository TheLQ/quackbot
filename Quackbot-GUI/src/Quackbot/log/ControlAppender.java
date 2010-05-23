package Quackbot.log;

/**
 * @(#)ControlAppender.java
 *
 * This file is part of Quackbot
 */
import Quackbot.Bot;
import Quackbot.GUI;
import javax.swing.SwingUtilities;

import org.apache.log4j.AppenderSkeleton;

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
				WriteOutput.writeStd(event);
			return;
		}


		if (GUIExists())
			SwingUtilities.invokeLater(new WriteOutput(GUI.instance.CerrorLog, this, event));
		else
			WriteOutput.writeStd(event);
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
}
