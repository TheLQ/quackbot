/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import Quackbot.plugins.JavaPlugin;
import Quackbot.plugins.impl.JavaTest;

/**
 *
 * @author lordquackstar
 */
public class Main {
	public static void main(String[] args) {
		Controller ctrl = new Controller();
		ctrl.connectDB("quackbot", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/quackbot", null, null, "root", null);
		ctrl.setDatabaseLogLevel(java.util.logging.Level.OFF);
		ctrl.addPlugin(new JavaPlugin(JavaTest.class.getName()));
		//ctrl.addPlugin(new JavaPlugin(HookTest.class.getName()));
		ctrl.start();
	}
}
