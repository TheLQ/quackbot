/*
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Utility class webTalk, gets online webpage
 */

var util = true;

function webTalk(url) {
	var netPkgs = new JavaImporter(java.io,java.net);
	with (netPkgs) {
		try {
			log.debug("Visiting url: "+url);

			// Get the response
			var rd = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
			var allLine = "";
			var line = "";
			while ((line = rd.readLine()) != null)
				allLine = allLine+line;
			rd.close();

			log.debug("Done visiting url");
			return  allLine;
		} catch (err) {
			log.error("ERROR "+err);
			if(err.toString().search("Connection refused: connect") != -1) {
				return null;
			}
		}
		}
}
