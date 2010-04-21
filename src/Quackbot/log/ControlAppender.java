/**
 * @(#)ControlAppender.java
 *
 * This file is part of Quackbot
 */
package Quackbot.log;

import Quackbot.InstanceTracker;

import org.apache.log4j.AppenderSkeleton;

import org.apache.log4j.spi.LoggingEvent;

/**
 * Appender for everything thats not bot. All events from Bot are ignored
 * @author Lord.Quackstar
 */
public class ControlAppender extends AppenderSkeleton {

	String[] BLOCK = new String[]{"Bot", "org.jibble"};
	WriteOutput out;

	public void append(LoggingEvent event) {
		//First make sure that this is comming from the right class
		String fullClass = event.getLocationInformation().getClassName();
		for (String search : BLOCK)
			if (fullClass.indexOf(search) != -1)
				return;

		InstanceTracker.getMainInst().log_threadpool.execute(new WriteOutput(InstanceTracker.getMainInst().CerrorLog,this,event));
	}

	public boolean requiresLayout() {
		return false;
	}

	public void close() {
		//nothing to close
	}
}
