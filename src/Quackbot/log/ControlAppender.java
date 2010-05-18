/**
 * @(#)ControlAppender.java
 *
 * This file is part of Quackbot
 */
package Quackbot.log;

import Quackbot.InstanceTracker;
import com.mysql.jdbc.StringUtils;
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
	public void append(LoggingEvent event) {
		if(InstanceTracker.mainExists())
			SwingUtilities.invokeLater(new WriteOutput(InstanceTracker.getMain().CerrorLog,this,event));
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
