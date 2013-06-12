/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.hooks.events;

import lombok.Getter;
import org.quackbot.Controller;
import org.quackbot.hooks.Command;

/**
 *
 * @author Leon
 */
@Getter
public class CommandAddedEvent extends QEvent {
	protected final Command command;
	protected final boolean added;

	public CommandAddedEvent(Controller controller, Command command, boolean added) {
		super(controller);
		this.command = command;
		this.added = added;
	}

	@Override
	@Deprecated
	public void respond(String response) {
		throw new UnsupportedOperationException("Cannot respond to CommandNewEvent");
	}
	
}
