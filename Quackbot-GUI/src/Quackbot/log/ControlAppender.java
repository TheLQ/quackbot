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
