/**
 * @(#)ThreadMgr.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
public class ThreadMgr {
	private static ExecutorService mainPool = Executors.newCachedThreadPool();

	/**
	 * Adds a runnable to the Main Queue
	 * @param rbl Runnable object
	 */
	public static synchronized void addMain(Runnable rbl) {
		mainPool.execute(rbl);
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
	 * Fetches main thread pool
	 * <u>This method is meant to be used internally</u>
	 * @return Main thread pool
	 */
	public static synchronized ExecutorService getMain() {
		return mainPool;
	}

	public static synchronized ExecutorService newBotPool(final String address) {
		return Executors.newCachedThreadPool(new ThreadFactory() {
			int threadCounter = 0;
			List<String> usedNames = new ArrayList<String>();
			ThreadGroup threadGroup;


			public Thread newThread(Runnable rbl) {
				String goodAddress = address;

				int counter = 0;
				while (usedNames.contains(goodAddress))
					goodAddress = address + "-"+(counter++);

				if(threadGroup == null)
					threadGroup = new ThreadGroup("quackbot-"+goodAddress);
				return new Thread(threadGroup, rbl, "quackbot-" + goodAddress + "-" + threadCounter++);
			}
		});
	}
}
