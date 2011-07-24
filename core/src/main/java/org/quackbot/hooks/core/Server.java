package org.quackbot.hooks.core;

import java.util.ArrayList;
import java.util.List;
import org.quackbot.Bot;
import org.quackbot.Controller;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.java.AdminOnly;
import org.quackbot.hooks.java.HelpDoc;
import org.quackbot.hooks.java.Optional;

/**
 *
 * @author lordquackstar
 */
@AdminOnly
@HelpDoc("The umbrilla command for any action to a server")
public class Server extends Command {
	public String onCommand(CommandEvent event, String action, String target, @Optional String[] arg2) throws Exception {
		Bot bot = event.getBot();
		Controller controller = bot.getController();

		if (action.equalsIgnoreCase("list")) {
			ArrayList<String> serverNames = new ArrayList(controller.getBots().size());
			for (Bot curBot : controller.getBots())
				serverNames.add(curBot.getServer());
			return "Connected to servers: " + serverNames.toString().substring(0, serverNames.toString().length());
		} else if (action.equalsIgnoreCase("add")) {
			controller.addServer(target);
			return "Attempting to join server: " + target;
		} else
			return "Unknown operation: " + action;
	}
}
