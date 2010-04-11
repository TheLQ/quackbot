package Quackbot.log;

import Quackbot.Main;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author admins
 */
public class ControlAppender extends AppenderSkeleton {

	String[] BLOCK = new String[]{"Bot", "org.jibble"};
	Main mainInst;
	WriteOutput out;

	public ControlAppender(Main mainInst) {
		this.mainInst = mainInst;
		out = new WriteOutput(mainInst.CerrorLog);
	}

	public void append(LoggingEvent event) {
		//First make sure that this is comming from the right class
		/*String fullClass = event.getLocationInformation().getClassName();
		boolean found = false;
		for (String search : BLOCK) {
			if (fullClass.indexOf(search) != -1) {
				found = true;
				break;
			}
		}
		if (found) {
			return; //Ignore, this only for bots
		}*/

		//If this is JackRabbit, ignore debug messages
		/*if (fullClass.indexOf("org.apache.jackrabbit") != -1 || fullClass.indexOf("org.apache.commons.beanutils") != -1) {
			if (event.getLevel().equals(Level.DEBUG)) {
				return;
			}
		}*/
		System.out.println(event.getMessage());
		out.write(event);
	}

	public boolean requiresLayout() {
		return false;
	}

	public void close() {
		//nothing to close
	}
}
