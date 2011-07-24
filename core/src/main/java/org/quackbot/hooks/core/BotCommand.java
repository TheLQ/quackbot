
package org.quackbot.hooks.core;

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
@HelpDoc("The umbrilla command for any action on the bot")
public class BotCommand extends Command {
	public String onCommand(CommandEvent event, String action, String target, @Optional String[] arg2) throws Exception {
		Bot bot = event.getBot();
		Controller controller = bot.getController();
		
		if(action.equalsIgnoreCase("reload")) {
			controller.reloadPlugins();
			return "Succesfully reloaded plugins";
		}
		
		throw new RuntimeException("Unknown operation: " + action);
	}
}
