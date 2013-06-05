/**
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
package org.quackbot.hooks;

/**
 * Generic hook loading interface. Allows hooks to be written in other languages
 * and loaded at runtime.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface HookLoader {
	/**
	 * Given the location of a file, load it as a {@link Hook} or as a 
	 * {@link Command}. Note that Command is a much more 
	 * specialized class and needs to be treated as such.
	 * <p>
	 * This is only called for files that match the extension given to 
	 * {@link org.quackbot.Controller}
	 * @param fileLocation The location of the file.
	 * @throws Exception   Any exception encountered while parsing. The hook will
	 *                     not be added and the exception will be logged
	 */
	public QListener load(String fileLocation) throws Exception;
}
