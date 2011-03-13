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
var help = "Starts countdown of specified length. Syntax: ?simpleCountdown <seconds>";

var futureMs = 0;
var msRemain = 0;
var closestMin = 0;

importClass(java.lang.System);

function onCommand(seconds) {
	futureMs = System.currentTimeMillis()+(seconds*1000);
	try{
		recalculate();
		while(msRemain > 60000) {
			getBot().sendMessage(timeRemaining(futureMs)+" remaining! (min countdows)");
			if(closestMin < 900)
				continue;
			log.debug("sleeping for "+closestMin+" | msremain: "+msRemain)
			Thread.sleep(closestMin)
			recalculate();
		}

		//Now at 1 min
		getBot().sendMessage(channel, "1 minuite remaining! (hard coded)");

		//Wait for 30 sec
		Thread.sleep(30000)
		qb.sendMsg(new BotMessage(event,"30 seconds remaining! (hard coded)"));

		//Wait for 20 sec
		Thread.sleep(20000)
		qb.sendMsg(new BotMessage(event,"20 seconds remaining! (hard coded)"));

		//Wait for 10 sec
		Thread.sleep(30000)
		qb.sendMsg(new BotMessage(event,"10 seconds remaining! (hard coded)"));

		//Wait for 5 sec
		Thread.sleep(5000)
		for(var i=5;i>0;i--) {
			qb.sendMsg(new BotMessage(event,i+" seconds remaining! (hard coded)"));
			Thread.sleep(1000);
		}

		qb.sendMsg(new BotMessage(event,"Whoo, end!!"));
	}
	catch(err) {
		log.error("ERROR "+err);
		if(err.toString().search("InterruptedException") != -1) {
			log.warn("Countdown thread interrupted");
			return;
		}
	}
}

function recalculate() {
	msRemain = futureMs-System.currentTimeMillis();
	closestMin = msRemain % 60000;
}