/**
 * @(#)Utils.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.info.JSPlugin;
import Quackbot.info.JavaPlugin;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 * Static Utility class.
 *
 * @author Lord.Quackstar
 */
public class Utils {
	
	/**
	 * Utility to find Java command in list case insitivly
	 * @param slist List to search
	 * @param find  What to find
	 * @return      Value found in list, null if not found
	 */
	public static JavaPlugin findJavaPlugin(String find) {
		List<JavaPlugin> slist = InstanceTracker.getController().javaPlugins;
		for (JavaPlugin curItem : slist)
			if (StringUtils.containsIgnoreCase(curItem.getName(), find))
				return curItem;
		return null;
	}

	/**
	 * Utility to find Java command in list case insitivly
	 * @param slist List to search
	 * @param find  What to find
	 * @return      Value found in list, null if not found
	 */
	public static JSPlugin findJSPlugin(String find) {
		Set<Map.Entry<String, JSPlugin>> slist = InstanceTracker.getController().JSplugins.entrySet();
		for (Map.Entry<String, JSPlugin> curItem : slist)
			if (StringUtils.containsIgnoreCase(curItem.getKey(), find))
				return curItem.getValue();
		return null;
	}
}
