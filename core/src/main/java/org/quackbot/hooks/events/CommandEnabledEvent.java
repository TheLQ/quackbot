/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.hooks.events;

import com.google.common.base.Preconditions;
import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2;
import lombok.Getter;
import org.quackbot.Controller;
import org.quackbot.hooks.Command;

/**
 *
 * @author Leon
 */
@Getter
public class CommandEnabledEvent extends QEvent {
	protected final Command command;
	protected final boolean oldEnabled;
	protected final boolean enabled;

	public CommandEnabledEvent(Controller controller, Command command, boolean oldEnabled, boolean enabled) {
		super(controller);
		this.command = command;
		this.oldEnabled = oldEnabled;
		this.enabled = enabled;
	}

	@Override
	public void respond(String response) {
		throw new UnsupportedOperationException("Cannot respond to CommandUpdatedEvent");
	}	
}
