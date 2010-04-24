/**
 * @(#)ThreadPoolManager.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Static class holding all used threadPools.
 * 
 * <ul>
 * <li>Main Pool - All core quackbot classes (execpt plugins) should be placed here
 * <li>Plugin Pool - All plugins that should be executed go here
 * <li>Logging pool - Logging queue pool. Should only be used by WriteOutput
 *                WARNING: This only contains one thread.
 *
 * @author Lord.Quackstar
 */
public class ThreadPoolManager {
	public static ExecutorService mainPool = Executors.newCachedThreadPool();
	public static ExecutorService pluginPool = Executors.newCachedThreadPool();

	/**
	 * Adds a runnable to the Main Queue
	 * @param rbl Runnable object
	 */
	public static synchronized void addMain(Runnable rbl) {
		mainPool.execute(rbl);
	}

	/**
	 * Adds plugin to plugin Queue
	 * @param rbl Runnable object
	 */
	public static synchronized void addPlugin(Runnable rbl) {
		pluginPool.execute(rbl);
	}

	/**
	 * Restarts Main pool <b>VERY DANGEROUS</b>
	 * <u>This method is meant to be used internally</u>
	 *
	 * WARNING: This will kill the Controller instance and all bots, making
	 * the pool completly empty. The controller must be restarted immediatly
	 * after running this method
	 */
	public static synchronized void restartMain() {
		mainPool.shutdownNow();
		mainPool = Executors.newCachedThreadPool();
	}

	/**
	 * Restarts down Plugin pool <b>VERY DANGEROUS</b>
	 * <u>This method is meant to be used internally</u>
	 *
	 * WARNING: This will kill all running plugins unless they are in an infinate loop
	 */
	public static synchronized void restartPlugin() {
		pluginPool.shutdownNow();
		pluginPool = Executors.newCachedThreadPool();
	}

	/**
	 * Fetches main thread pool
	 * <u>This method is meant to be used internally</u>
	 * @return Main thread pool
	 */
	public static synchronized ExecutorService getMain() {
		return mainPool;
	}

	/**
	 * Fetches plugin thread pool
	 * <u>This method is meant to be used internally</u>
	 * @return plugin thread pool
	 */
	public static synchronized ExecutorService getPlugin() {
		return pluginPool;
	}
}
