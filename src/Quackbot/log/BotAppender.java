/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.log;

import Quackbot.InstanceTracker;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author admins
 */
public class BotAppender extends AppenderSkeleton {

	String[] APPROVED = new String[]{"Bot", "org.jibble"};
	WriteOutput out;
	String address;

	public BotAppender(String address) {
		this.out = new WriteOutput(InstanceTracker.getMainInst().BerrorLog);
		this.address = address;
	}

	public void append(LoggingEvent event) {
		out.write(event, address);
	}

	public boolean requiresLayout() {
		return false;
	}

	public void close() {
		//nothing to close
	}
}
