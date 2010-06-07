package Quackbot.impl;

import Quackbot.Bot;
import Quackbot.Controller;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SandBox {
	public SandBox() {
		ExecutorService msgQueue = Executors.newSingleThreadScheduledExecutor();
		for (int i = 0; i < 10; i++) {
			try {
				msgQueue.submit(new Runnable() {
					public void run() {
						System.out.println("I'm free!!!");
					}
				}).get();
				//Add a seperate wait so next runnable doesn't get executed yet but
				//above one unblocks
				msgQueue.submit(new Runnable() {
					public void run() {
						try {
							System.err.println("Waiting");
							Thread.sleep(Controller.msgWait);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).get();

			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new SandBox();

	}
}
