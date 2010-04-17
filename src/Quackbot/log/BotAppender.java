/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.log;

import Quackbot.Main;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author admins
 */
public class BotAppender extends AppenderSkeleton {

	String[] APPROVED = new String[]{"Bot", "org.jibble"};
	Main mainInst;
	WriteOutput out;
	String address;

	public BotAppender(Main mainInst,String address) {
		this.mainInst = mainInst;
		this.out = new WriteOutput(mainInst.BerrorLog);
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
