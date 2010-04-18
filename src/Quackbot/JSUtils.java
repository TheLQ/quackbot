/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot;

import Quackbot.info.JSCmdInfo;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author admins
 */
public class JSUtils {
	public static TreeMap<String,JSCmdInfo> getUtils() {
		Controller ctrl = InstanceTracker.getCtrlInst();
		TreeMap<String,JSCmdInfo> listeners = new TreeMap<String,JSCmdInfo>();
		//Set<Map.Entry<String,JSCmdInfo>> cmdSet = ctrl.JSCmds.entrySet();
		for(Map.Entry<String,JSCmdInfo> curCmd : ctrl.JSCmds.entrySet())
			if(curCmd.getValue().isUtil())
				listeners.put(curCmd.getKey(), curCmd.getValue());
		return listeners;
	}

	public static TreeMap<String,JSCmdInfo> getServices() {
		Controller ctrl = InstanceTracker.getCtrlInst();
		TreeMap<String,JSCmdInfo> listeners = new TreeMap<String,JSCmdInfo>();
		//Set<Map.Entry<String,JSCmdInfo>> cmdSet = ctrl.JSCmds.entrySet();
		for(Map.Entry<String,JSCmdInfo> curCmd : ctrl.JSCmds.entrySet())
			if(curCmd.getValue().isService())
				listeners.put(curCmd.getKey(), curCmd.getValue());
		return listeners;
	}

	public static TreeMap<String,JSCmdInfo> getListeners() {
		Controller ctrl = InstanceTracker.getCtrlInst();
		TreeMap<String,JSCmdInfo> listeners = new TreeMap<String,JSCmdInfo>();
		//Set<Map.Entry<String,JSCmdInfo>> cmdSet = ctrl.JSCmds.entrySet();
		for(Map.Entry<String,JSCmdInfo> curCmd : ctrl.JSCmds.entrySet())
			if(curCmd.getValue().isListener())
				listeners.put(curCmd.getKey(), curCmd.getValue());
		return listeners;
	}
}
