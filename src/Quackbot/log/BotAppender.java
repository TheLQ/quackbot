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
 * @author Lord.Quackstar
 */
public class BotAppender extends AppenderSkeleton {

	/**
	 * Output writter instance
	 */
	WriteOutput out;
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

	public void append(LoggingEvent event) {
		if (InstanceTracker.mainExists())
			SwingUtilities.invokeLater(new WriteOutput(InstanceTracker.getMain().BerrorLog, this, event, address));
		else
			WriteOutput.writeStd(event);
	}

	public boolean requiresLayout() {
		return false;
	}

	public void close() {
		//nothing to close
	}
}
