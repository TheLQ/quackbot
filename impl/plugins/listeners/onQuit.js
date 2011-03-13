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
 * For the lyokofreak stream, tells users who can't see it that a new person has quit
 */

var hook = Event.onQuit;

function invoke() {
//log.debug("Channel: "+event.channel+" | Bot: "+qb.isBot(event)+" | Result: "+(qb.isBot(event) && qb.getServer().indexOf("ustream") != -1));
//	if(qb.isBot(event) && qb.getServer().indexOf("ustream") != -1) {
		//Yes this is blind, but there is no way to tell what channel the user was on
		qb.sendMsg(new BotMessage("#lyokofreak-viewing-party","User "+event.sender+" has quit"));
//	}
}
