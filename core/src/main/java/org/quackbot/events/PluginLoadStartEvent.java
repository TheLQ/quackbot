

package org.quackbot.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.hooks.Event;
import org.quackbot.Controller;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PluginLoadStartEvent extends Event {
	private final Controller controller;
	
	public PluginLoadStartEvent(Controller ctrl) {
		super(null);
		this.controller = ctrl;
	}
}