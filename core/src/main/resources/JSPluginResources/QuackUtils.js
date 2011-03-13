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
importPackage(Packages.Quackbot);
importPackage(Packages.Quackbot.info);
importPackage(Packages.Quackbot.hook);
importPackage(Packages.Quackbot.err);
importClass(Packages.java.lang.Thread);
var QuackUtils = {
	toJavaArray: function(type, arr) {
		var jArr;
		if(arr.length) {
			jArr = java.lang.reflect.Array.newInstance(type, arr.length);
			for(var i=0;i<arr.length;i++)
				jArr[i] = arr[i];
		}
		else {
			jArr = java.lang.reflect.Array.newInstance(type, 1);
			jArr[0] = arr;
		}
		return jArr;
	},
	isArray: function(object) {
		return object.length && typeof object != 'string';
	},
	onCommandParse: function(theOnCommand) {
		if(theOnCommand.toSource() == "function onCommand() {return null;}")
			return 0;
		return theOnCommand.arity;
	},
	stringClass: new java.lang.String().getClass()
}