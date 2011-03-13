/*
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of PircBotX.
 *
 * PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */
/* 
 * Reports what VLC is currently playing
 */

importClass(java.lang.Thread);

var service = true;

//Start checking loop in another thread
function invoke() {
	log.debug("Initalizing vlc update");
	var previous = "";
	while(1) {
		try{
			Thread.sleep(5000);
			log.debug("Checking");
			var current =  webTalk("http://localhost:8082/current.html");
			if(current == null) {
				log.warn("Unable to connect to VLC, reload CMDs to retry");
				return;
			}
			if(current != previous)
				ctrl.sendGlobalMessage(current);
			else
				log.debug("Nothing to do");
			previous = current;
		}
		catch(err) {
			log.error("ERROR "+err);
			if(err.toString().search("InterruptedException") != -1) {
				log.warn("VLC thread interrupted");
			}
			return;
		}
	}
}