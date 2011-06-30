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
var name, help = "", admin = false, enabled = true, file, alreadySetup = false;

function getRequiredParams() {
	var specified = 0;
	var onCommandSpec = QuackUtils.onCommandParse(onCommand);
	if(typeof parameters == 'object') {
		//See if its an array, declared with [5, 0]
		if(QuackUtils.isArray(parameters))
			specified = parameters[0]
		//See if its an object, declared with {required: 5, optional: 0}
		else if(typeof parameters.required != 'undefined')
			specified =  parameters.required;
	}
	//Must be a number
	else if(typeof parameters != 'undefined')
		specified = parameters;

	//Subtract the optional if nessesary
	optional = getOptionalParams();
	if(specified != 0) //If manually specified, return it
		return specified;
	else if(optional != 0 && optional != -1 && onCommandSpec != 0) //If only optional is specified, use that
		return onCommandSpec - optional;
	//Only left with onCommand or 0 (account for event param)
	return onCommandSpec - 1;
}

function getOptionalParams() {
	var specified = 0;
	if(typeof(parameters) == 'object')
		//See if its an array, declared with [5, 0]
		if(QuackUtils.isArray(parameters))
			specified = parameters[1]
		//See if its an object, declared with {required: 5, optional: 0}
		else if(typeof parameters.optional != 'undefined')
			specified = parameters.optional;
	//You can't label individual params as optional, so it can only be specified
	return specified;
}