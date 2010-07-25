/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.impl;

import Quackbot.Controller;
import Quackbot.plugins.JavaPluginLoader;
import Quackbot.plugins.impl.JavaTest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.LoggerFactory;

/**
 * Main Class for Implmentation.
 *
 * @author LordQuackstar
 */
public class Main {
	/**
	 * Main method of Implementation
	 * @param args Passed parameters (ignored)
	 */
	public static void main(String[] args) {
		Controller ctrl = new Controller();

		String[] dbInfo = getDBInfo();
		//This implementation uses the Commons DBCP for Connection managment.
		//This is not nessesary for most applications
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUsername(dbInfo[1]);
		ds.setPassword(dbInfo[2]);
		ds.setUrl(dbInfo[0] + "?autoReconnect=true");
		ds.setValidationQuery("SELECT * FROM quackbot_server");
		ds.setTestOnBorrow(true);

		ctrl.connectDB(dbInfo[0], 10, ds, null, null);
		ctrl.setDatabaseLogLevel(java.util.logging.Level.OFF);
		JavaPluginLoader.load(new JavaTest());
		//ctrl.addCommand(new JavaPluginLoader(HookTest.class.getName()));
		ctrl.addPrefix("?");
		ctrl.start();
	}

	/**
	 * Get all DB info from uncommited file. So no, you can't get access to my database :-)
	 * @return String array with values: databasename, db connection string, username, and password
	 */
	public static String[] getDBInfo() {
		try {
			return new BufferedReader(new FileReader("mysqlPasswords.txt")).readLine().split(",");
		} catch (Exception e) {
			LoggerFactory.getLogger(Main.class).error("Cannot find mysqlPasswords.txt in dir " + (new File("").getAbsolutePath()), e);
		}
		return null;
	}
}
