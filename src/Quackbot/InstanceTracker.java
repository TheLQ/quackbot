/**
 * @(#)InstanceTracker.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

/**
 * This Utility class keeps track of the Main and Controller instances.
 * This way there is only one of each and you don't have to do ugly instance chaining to get them
 * @author Lord.Quackstar
 */
public class InstanceTracker {

	/**
	 * Main instance. Get with {@link #getMain()}
	 */
	private static Main mainInst = null;
	/**
	 * Controller instance. Get with {@link #getController()}
	 */
	private static Controller ctrlInst = null;

	/**
	 * Fetches the only Main instance
	 * @return The Main instance
	 */
	public static Main getMain() {
		return mainInst;
	}

	/**
	 * <b>WARNING</b> CALLING THIS OUTSIDE OF MAIN.JAVA WILL <i>BREAK</i> THE BOT
	 * This method should <u>never</u> be called, Main.java automatically sets it
	 *
	 * Sets the Main instance
	 * @param aMainInst The Main instance being used
	 */
	public static void setMain(Main aMainInst) {
		mainInst = aMainInst;
	}

	/**
	 * Fetches the only Controller Instance
	 * @return The Controller instance
	 */
	public static Controller getController() {
		return ctrlInst;
	}

	/**
	 * <b>WARNING</b> CALLING THIS OUTSIDE OF CONTROLLER.JAVA WILL <i>BREAK</i> THE BOT
	 * This method should <u>never</u> be called, Controller.java automatically sets it
	 *
	 * Sets the only Controller instance
	 * @param aCtrlInst The contoller instance being used
	 */
	public static void setController(Controller aCtrlInst) {
		ctrlInst = aCtrlInst;
	}
}
