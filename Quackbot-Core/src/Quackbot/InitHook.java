/**
 * @(#)InitHook.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

/**
 * An interface for adding Hooks to the Quackbot start process. Its recommended to be used
 * inside of a static block so its added immediatly. Once {@link Controller#start() } is called,
 * no more hooks can be added
 * @author lordquackstar
 */
public interface InitHook {
	/**
	 * Method called on Quackbot start process
	 * @param ctrl Controller instance
	 */
	public void run(Controller ctrl);
}
