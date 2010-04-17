package Quackbot.log;

import Quackbot.InstanceTracker;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author admins
 */
public class ControlAppender extends AppenderSkeleton {

	String[] BLOCK = new String[]{"Bot", "org.jibble"};
	WriteOutput out;

	public ControlAppender() {
		out = new WriteOutput(InstanceTracker.getMainInst().CerrorLog);
	}

	public void append(LoggingEvent event) {
		//First make sure that this is comming from the right class
		String fullClass = event.getLocationInformation().getClassName();
		for (String search : BLOCK)
			if (fullClass.indexOf(search) != -1)
				return;

		out.write(event);
	}

	public boolean requiresLayout() {
		return false;
	}

	public void close() {
		//nothing to close
	}
}
