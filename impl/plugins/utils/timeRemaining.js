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
 * Count down until any date script-
 * By JavaScript Kit (www.javascriptkit.com)
 * Over 200+ free scripts here!
 *
 * Simple utility to calculate time remaining
*/

var util = true;

function timeRemaining(futureMs){
	var realFutureMs = futureMs + 1000; //extra second to get better human rediable results
	dd=realFutureMs-System.currentTimeMillis()
	dday=Math.floor(dd/(60*60*1000*24)*1)
	dhour=Math.floor((dd%(60*60*1000*24))/(60*60*1000)*1)
	dmin=Math.floor(((dd%(60*60*1000*24))%(60*60*1000))/(60*1000)*1)
	dsec=Math.floor((((dd%(60*60*1000*24))%(60*60*1000))%(60*1000))/1000*1)
	var finalString = "";
	var minStr = "";
	if(dmin == 1)
		minStr = "minuite"
	else
		minStr = "minuites"

	if(dday==0&&dhour==0&&dmin==0&&dsec==1)
		return null
	if(dday != 0)
		finalString=finalString+dday+ " days, "
	if(dhour != 0)
		finalString=finalString+dhour+" hours, "
	if(dmin != 0 && dsec != 0)
		finalString=finalString+dmin+" "+minStr+", and "+dsec+" seconds";
	else if(dmin != 0 && dsec == 0)
		finalString=finalString+dmin+" "+minStr;
	else
		finalString=finalString+dsec+" seconds";
	return finalString;
}