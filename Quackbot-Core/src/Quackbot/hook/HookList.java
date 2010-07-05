package Quackbot.hook;

import Quackbot.Bot;
import Quackbot.info.BotEvent;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookList<A,B> extends ArrayList<PluginHook> {
	public boolean interupted;
	public List<PluginHook> ignored;
	public boolean needsSuper;
	private Logger log = LoggerFactory.getLogger(HookList.class);

	public HookList() {
		super();
	}

	public void startStack(Bot bot, BotEvent<A,B> msgInfo) throws Exception {
		//Reset all class vars
		interupted = needsSuper = false;
		ignored = new ArrayList<PluginHook>();

		//Execute stack
		for (PluginHook plugin : this) {
			if(!ignored.contains(plugin))
				plugin.run(this, bot, msgInfo);
			else if(interupted)
				break;
		}
	}

	public void ignore(int position) {
		ignore(get(position));
	}

	public void ignore(PluginHook hook) {
		ignored.add(hook);
	}

	public void stopStack() {
		interupted = true;
	}
}
