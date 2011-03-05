/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.plugins.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class level annotation that provides help for a class. Not required but highly recommended
 * <p>
 * Note: Syntax information is automatically provided.
 * <p>
 * Example:
 * <pre>
 * Help("Generic test class just to see if it works")
 * public class someClass extends BasePlugin {
 *	...
 * }}
 * </pre>
 * @author Lord.Quackstar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HelpDoc {
	/**
	 * Help string that will be given to user upon request. It is highly
	 * recommended that every Java Plugin (and plugin's in general) need
	 * to include.
	 * @return Given help string, or "No help available" when none is given
	 */
	String value() default "No help available";
}