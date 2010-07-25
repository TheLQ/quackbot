/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.plugins.core;

import Quackbot.Command;
import Quackbot.CommandManager;
import Quackbot.Controller;
import Quackbot.err.InvalidCMDException;
import Quackbot.plugins.java.AdminOnly;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.plugins.java.Parameters;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
@Parameters(optional = 1)
@HelpDoc("Provides list of Admin-only commands or help for specific command. Syntax: ?help <OPTIONAL:command>")
@AdminOnly
public class AdminHelp extends Command {
	private static Logger log = LoggerFactory.getLogger(AdminHelp.class);
	Controller ctrl = Controller.instance;

	@Override
	public void onCommand(String channel, String sender, String login, String hostname, String[] args) throws Exception {
		//Does user want command list
		if (args[0] == null) {
			List<String> cmdList = new ArrayList<String>();

			//Add Java Plugins
			for (Command curCmd :CommandManager.getCommands())
				if (curCmd.isEnabled() && curCmd.isAdmin())
					cmdList.add(curCmd.getName());

			//Send to user
			getBot().sendMessage(channel, "Possible commands: " + StringUtils.join(cmdList.toArray(), ", "));
		} else {
			Command result = CommandManager.getCommand(args[0]);
			if (result == null)
				throw new InvalidCMDException(args[0]);
			else if (!result.isEnabled())
				throw new InvalidCMDException(args[0], "(disabled)");
			else if(StringUtils.isBlank(result.getHelp()))
				getBot().sendMessage(channel, sender, "No help avalible");
			else
				getBot().sendMessage(channel, sender, result.getHelp());
		}
	}
}
