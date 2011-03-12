
package org.quackbot.events;

import java.io.File;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.quackbot.Controller;
import org.quackbot.HookLoader;
import org.quackbot.hook.Hook;

/**
 * Created when a plugin is loaded. Contains either the loaded plugin or the
 * exception that occured when loading it
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HookLoadEvent extends Event {
	private final Controller controller;
	private final Hook hook;
	private final HookLoader pluginLoader;
	private final File file;
	private final Exception exception;

	public HookLoadEvent(Controller controller, Hook hook, HookLoader pluginLoader, File file, Exception exception) {
		super(null);
		this.controller = controller;
		this.pluginLoader = pluginLoader;
		this.file = file;
		this.exception = exception;
		this.hook = hook;
	}
}
