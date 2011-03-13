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
var help = "Sets the mode. Actions: ban, unban, kickban, set, kick, mute, op, deop, voice, devoice. Syntax: ?mode <action> <argument>"
var parameter = 2
var admin = true;

function onCommandChannel(channel, sender, login, hostname, arg) {
	var arg1 = arg[1];
	switch(arg[0]) {
		case "ban":
			var hostmask = parseHostmask(arg1);
			if(hostmask.indexOf("@") != -1) {
				getBot().ban(channel, hostmask);
				return "Banned user with hostmask"+hostmask;
			}
			return hostmask; //Contains error
		case "unban":
			var hostmask = parseHostmask(arg1);
			if(hostmask.indexOf("@") != -1) {
				getBot().ban(channel, hostmask);
				return "Unbanned hostmask "+hostmask;
			}
			return hostmask; //contains error
		case "kickban":
			var hostmask = parseHostmask(arg1);
			if(hostmask.indexOf("@") != -1) {
				getBot().ban(channel, hostmask);
				getBot().kick(channel, getUser(hostmask));
				return "Banned and kicked user "+getUser(hostmask)+" with hostmask"+hostmask;
			}
			return hostmask; //Contains error
		case "kick":
			//Just kick what ever is in the args
			getBot().kick(channel, arg1);
			return "Kicked "+arg1
		case "set":
			//Set what ever we are given
			getBot().setMode(channel,arg1);
			return "Set mode "+arg1
		case "mute":
			var timeMs = arg[2]*60000;
			var hostmask = parseHostmask(arg1);
			if(hostmask.indexOf("@") == -1)
				return hostmask; //Contains error

			getBot().ban(channel,hostmask);
			getBot().sendMessage(channel,getUser(hostmask),"You have been muted for "+timeMs);
			log.debug("Sleeping");
			Thread.sleep(timeMs);
			getBot().unBan(channel,hostmask);
			return user+" has been unmuted";
		case "op":
			//Check if were an op first
			if(getBot().userExists(getBot().getNick()))
				if(!getBot().getUser(channel,getBot().getNick()).isOp())
					return "Bot is not an op";

			//Verify the user exists
			if(!getBot().userExists(arg1))
				return "User does not exist";
			var user = getBot().getUser(channel,arg1);
			getBot().op(channel,arg1);
			Thread.sleep(1500); //sleep for 1.5 seconds waiting for response from the server
			if(user.isOp())
				return "Sucessfully Oped "+arg1;
			else
				return "Failed to op "+arg1+" (bot not an op?)";
		case "deop":
			//Check if were an op first
			if(getBot().userExists(getBot().getNick()))
				if(!getBot().getUser(channel,getBot().getNick()).isOp())
					return "Bot is not an op";

			//Verify the user exists
			if(!getBot().userExists(arg1))
				return "User does not exist";
			var user = getBot().getUser(channel,arg1);
			getBot().deOp(channel,arg1);
			Thread.sleep(1500); //sleep for 1.5 seconds waiting for response from the server
			if(!user.isOp())
				return "Sucessfully Deoped "+arg1;
			else
				return "Failed to deop "+arg1+" (bot not an op?)";
		case "voice":
			//Check if were an op first
			if(getBot().userExists(getBot().getNick()))
				if(!getBot().getUser(channel,getBot().getNick()).isOp())
					return "Bot is not an op";

			//Verify the user exists
			if(!getBot().userExists(arg1))
				return "User does not exist";
			var user = getBot().getUser(channel,arg1);
			getBot().voice(channel,arg1);
			Thread.sleep(1500); //sleep for 1.5 seconds waiting for response from the server
			if(user.hasVoice())
				return "Sucessfully voiced "+arg1;
			else
				return "Failed to voice "+arg1+" (bot not an op?)";
		case "devoice":
			//Check if were an op first
			if(getBot().userExists(getBot().getNick()))
				if(!getBot().getUser(channel,getBot().getNick()).isOp())
					return "Bot is not an op";

			//Verify the user exists
			if(!getBot().userExists(arg1))
				return "User does not exist";
			var user = getBot().getUser(channel,arg1);
			getBot().deVoice(channel,arg1);
			Thread.sleep(1500); //sleep for 1.5 seconds waiting for response from the server
			if(!user.hasVoice())
				return "Sucessfully voiced "+arg1;
			else
				return "Failed to voice "+arg1+" (bot not an op?)";
		default:
			return "Invalid action "+arg[0];
	}
	throw "Reached outside of switch, not supposed to happen";
}

function parseHostmask(givenHost) {
	var hostmask = "";
	if(arg1.indexOf("@") != -1 && arg1.indexOf("!") != -1) //Does it have the full hostmask?
		hostmask = arg1;
	else {//Assume only nick
		user = getBot().getUser(channel, arg1);
		if(user != null)
			hostmask = "*!"+user.getName()+"@"+user.getHostname();
		else
			return "User "+arg1+" does not exist";
	}
	return hostmask;
}

function getUser(hostmask) {
	var user;
	if(arg1.indexOf("@") != -1) //Do we need to strip out the
		user = arg1.split("!")[1];
	else
		user = arg1;
	return user;
}