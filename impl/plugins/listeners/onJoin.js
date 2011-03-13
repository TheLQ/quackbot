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
 * For the lyokofreak stream, tells users who can't see it that a new person has joined
 */

var hook = Event.onJoin;

function invoke() {
	log.debug("JS Result: "+event.toString());
	//log.debug("Channel: "+event.channel+" | Name: "+event.sender+" | Bot: "+qb.isBot(event)+" | Result: "+(event.channel=="#lyokofreak-viewing-party" && !qb.isBot(event)));
	if(event.channel=="#lyokofreak-viewing-party" && !qb.isBot(event) && qb.getServer().indexOf("ustream") != -1) {
		var prefix = "ustream";
		var msg_suffix = "";
		if(event.sender.substr(0,prefix.length)==prefix) {
			msg_suffix = " (please change nick with /nick yournickhere)"
		}
		qb.sendMsg(new BotMessage(event.getChannel(),"Welcome to the LyokoFreak Viewing Party "+event.getSender()+" "+msg_suffix));
		var current =  whatsPlaying();
		if(current.indexOf("ERROR") == -1)
			qb.sendMsg(new BotMessage(event,"Current song: "+whatsPlaying()));
		
	}
}
