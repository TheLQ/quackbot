/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import Quackbot.info.JavaPlugin;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author admins
 */
public class Utils {

	/**
	 * Utility to find a string inside of a list case-insisitvly
	 * @param slist List to search
	 * @param find  What to find
	 * @return      Value found in list, null if not found
	 */
	public static JavaPlugin findCI(List<JavaPlugin> slist, String find) {
		for (JavaPlugin curItem : slist)
			if (curItem.getName().equalsIgnoreCase(find))
				return curItem;
		return null;
	}
}
