/**
 * @(#)Utils.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import java.util.List;

/**
 * Static Utility class.
 *
 * @author Lord.Quackstar
 */
public class Utils {
	public static PluginType findPlugin(String find) {
		List<PluginType> slist = Controller.instance.plugins;
		for (PluginType curItem : slist)
			if (curItem.getName().equalsIgnoreCase(find))
				return curItem;
		return null;
	}
}
