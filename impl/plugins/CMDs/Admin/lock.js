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
importClass(org.apache.commons.lang.StringUtils);
var help = "Locks the bot globaly or by channel. Syntax: ?lock <OPTIONAL:channel>"
var parameter = {
	optional:1
};
var admin = true;

function onCommandChannel(channel, sender, login, hostname, args) {
	if(ars[0] == "this") {
		getBot().chanLockList.add(channel);
		return "Bot has been locked for this channel ("+channel+")";
	} else if(StringUtils.isNotBlank(args[0])) {
		getBot().chanLockList.add(args[0]);
		return "Bot has been locked for channel "+args[0];
	} else {
		getBot().botLocked = true;
		return "Bot has been locked globally";
	}
}