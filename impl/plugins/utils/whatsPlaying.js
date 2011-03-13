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
/*
 * Reports what VLC is currently playing
 */
var util = true;

function whatsPlaying() {
	log.debug("Initalizing vlc update");
	var msg = webTalk("http://127.0.0.1:8909/current.html");
	if(msg == null)
		return "ERROR: No media player is running";

	var msg = msg.split(",");
	if(msg[2] != "playing")
		return "ERROR: Music is not playing";

	return "Now playing "+msg[0]+" by "+msg[1];
}