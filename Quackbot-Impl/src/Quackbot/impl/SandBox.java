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

import Quackbot.info.Server;
import ejp.DatabaseManager;

public class SandBox {
	public static void main(String[] args) {
		DatabaseManager dbm = DatabaseManager.getDatabaseManager("evilxproductions", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://evilxproductions.db.3734136.hostedresource.com/evilxproductions", "evilxproductions", "vZsY3MN2g4vJqo");
		try {
			dbm.loadObject(new Server());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
//-javaagent:lib/jrebel.jar -noverify

