/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.hooks.Event;
import org.quackbot.Controller;

/**
 * ListenerManager has started processing events
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StartEvent extends Event {
	private final Controller controller;
	public StartEvent(Controller controller) {
		super(null);
		this.controller = controller;
	}
	
	@Override
	public void respond(String response) {
		throw new UnsupportedOperationException("Can't respond to a start event");
	}
}
