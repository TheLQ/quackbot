package Quackbot;

import Quackbot.info.UserMessage;
import java.util.Iterator;
import javax.script.Bindings;
import javax.script.ScriptContext;
import org.apache.log4j.Logger;

/**
 * Simple Runnable to run command in seperate thread
 *
 * @author Lord.Quackstar
 */
class threadCmdRun implements Runnable {

    String jsCmd;
    ScriptContext context;
    Controller mainInst;
    Bot bot = null;
    UserMessage msgInfo;
    Bindings scope;
    Logger log = Logger.getLogger(threadCmdRun.class);

    /**
     * Setup runnable
     * @param jsCmd
     * @param context
     */
    public threadCmdRun(String jsCmd, ScriptContext context, Controller mainInst) {
	this.jsCmd = jsCmd;
	this.context = context;
	this.mainInst = mainInst;
    }

    public threadCmdRun(String jsCmd, ScriptContext context, Bindings scope, Bot bot, UserMessage msgInfo) {
	this.jsCmd = jsCmd;
	this.context = context;
	this.mainInst = bot.mainInst;
	this.bot = bot;
	this.msgInfo = msgInfo;
	this.scope = scope;
    }

    /**
     * Run in background
     */
    public void run() {
	try {
	    if (bot != null) {
		scope.put("msgInfo", msgInfo);
		scope.put("qb", bot);
	    }
	    Iterator utilItr = mainInst.utils.iterator();
	    StringBuilder utilSB = new StringBuilder();
	    while (utilItr.hasNext()) {
		utilSB.append(utilItr.next());
	    }
	    mainInst.jsEngine.eval(utilSB.append(jsCmd).toString(), context);
	} catch (Exception e) {
	    if (bot != null) {
		bot.sendMessage(msgInfo.channel, msgInfo.sender + ": CMD ERROR: " + e.toString());
	    }
	    log.error("Error in CMD excecution", e);
	}
    }
}
