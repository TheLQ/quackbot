/**
 * @(#)BotAppender.java
 *
 * This file is part of Quackbot
 */
package Quackbot.log;

import Quackbot.InstanceTracker;
import javax.swing.SwingUtilities;

import org.apache.log4j.AppenderSkeleton;

import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j for Bot and Commands ONLY
 * 
 * @author Lord.Quackstar
 */
public class BotAppender extends AppenderSkeleton {

	/**
	 * Address of Bot server
	 */
	String address;

	/**
	 * Generate from Address
	 * @param address Address of server
	 */
	public BotAppender(String address) {
		this.address = address;
	}

	/**
	 * Used by Log4j to write something from the LoggingEvent. This simply points to
	 * WriteOutput which writes to the the GUI or to the console
	 * @param event
	 */
	public void append(LoggingEvent event) {
		if (InstanceTracker.mainExists())
			SwingUtilities.invokeLater(new WriteOutput(InstanceTracker.getMain().BerrorLog, this, event, address));
		else
			WriteOutput.writeStd(event);
	}

	/**
	 * Used by Log4j to determine if this requires a layout. Since all the dirty work is
	 * done by {@link WriteOutput}, this returns false;
	 */
	public boolean requiresLayout() {
		return false;
	}

	/**
	 * Used by Log4j to close anything that this Appender needs to close. Since this just
	 * writes to a JTextPane, this just does nothing
	 */
	public void close() {
		//nothing to close
	}
}
