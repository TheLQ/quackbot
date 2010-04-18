/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import Quackbot.err.AdminException;
import Quackbot.err.InvalidCMDException;
import Quackbot.err.NumArgException;
import Quackbot.info.BotMessage;
import Quackbot.info.JSCmdInfo;
import Quackbot.info.UserMessage;
import Quackbot.plugins.java.JavaCmdTpl;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import org.apache.log4j.Logger;

/**
 *
 * @author admins
 */
public class PluginExecutor implements Runnable {

	String command;
	String[] params;
	Bot qb;
	UserMessage msgInfo;
	Controller ctrl = InstanceTracker.getCtrlInst();
	Logger log = Logger.getLogger(PluginExecutor.class);
	//Public vars for threaded js running
	ScriptContext newContext;
	String jsCommand;

	public PluginExecutor(Bot bot, UserMessage msgInfo, String command, String[] params) {
		this.command = command;
		this.params = params;
		this.msgInfo = msgInfo;
		this.qb = bot;
	}

	public PluginExecutor(String command, String[] params) {
		this.command = command;
		this.params = params;
	}

	public void run() {
		log.warn("Running Plugin Excecutor for " + command);
		try {
			String javaResult = findCI(ctrl.javaPlugins,"Quackbot.plugins.java." + command);
			if (ctrl.JSCmds.keySet().contains(command))
				runJs();
			else if (javaResult != null)
				runJava(javaResult);
			else
				throw new InvalidCMDException(command);
		} catch (AdminException e) {
			log.error("Person is not admin!!", e);
			if (qb != null)
				qb.sendMsg(new BotMessage(msgInfo, e));
		} catch (NumArgException e) {
			log.error("Wrong params!!!", e);
			if (qb != null)
				qb.sendMsg(new BotMessage(msgInfo, e));
		} catch (Exception e) {
			log.error("Other error", e);
			if (qb != null)
				qb.sendMsg(new BotMessage(msgInfo, e));
		}
	}

	private void runJs() throws Exception {
		JSCmdInfo cmdInfo = ctrl.JSCmds.get(command);
		//Is this an admin function? If so, is the person an admin?
		if (cmdInfo.isAdmin() && !qb.isAdmin(msgInfo.sender))
			throw new AdminException();

		//Does this method require args?
		if (cmdInfo.isReqArg() && params.length == 0) {
			log.debug("Method does require args, passing length 1 array");
			params = new String[1];
		}

		//Does the required number of args exist?
		int user_args = params.length;
		int method_args = cmdInfo.getParams();
		log.debug("User Args: " + user_args + " | Req Args: " + method_args);
		if (user_args != method_args)
			throw new NumArgException(user_args, method_args);

		//All requirements are met, excecute method
		log.info("All tests passed, running method");
		newContext = (ScriptContext) cmdInfo.getContext();
		Bindings engineScope = (Bindings) cmdInfo.getScope();
		if (qb != null) {
			engineScope.put("msgInfo", msgInfo);
			engineScope.put("qb", qb);
		}
		engineScope.put("log", Logger.getLogger("Quackbot.plugins.js." + cmdInfo.getName()));

		//build command string
		StringBuilder jsCmd = new StringBuilder();
		jsCmd.append("invoke( ");
		for (String arg : params)
			jsCmd.append(" '" + arg + "',");
		jsCmd.deleteCharAt(jsCmd.length() - 1);
		jsCmd.append(");");

		jsCommand = jsCmd.toString();

		log.debug("JS cmd: " + jsCommand);

		//Run command in thread pool
		InstanceTracker.getCtrlInst().jsEngine.eval(jsCommand, newContext);
	}

	private void runJava(String javaLoc) throws Exception {
		JavaCmdTpl javaCmd = (JavaCmdTpl)this.getClass().getClassLoader().loadClass(javaLoc).newInstance();
		javaCmd.invoke(qb, msgInfo);
	}

	private String findCI(List<String> slist, String find) {
		for(String curItem : slist)
			if(curItem.equalsIgnoreCase(find))
				return curItem;
		return null;
	}
}