/**
 * @(#)ParamNum.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
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
package Quackbot.plugins.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation that defines how many parameters this Java plugin requires as a number.
 * <p>
 * This used to determine if the correct number of parameters have been passed by the user
 * and to generate the syntax in the command help
 * <p>
 * This annotation is <u>not preferred</u> for long term usage, only for quick short
 * term usage. If used then the command must manually go through the arguments list and manually
 * make syntax help in the command line.
 * Parameters cannot be optional if using this. While taking up more space,
 * {@link ParamConfig} is preferred.
 * <p>
 * An example on how to use this on a command that takes 1 parameter
 * <br>
 * <pre>
 * &#064;ParamNum(1)
 * public class someClass extends BasePlugin {
 *	...
 * }}
 * </pre>
 * <p>
 * This annotation is not required if there are 0 parameters, however it
 * is best practice to include it, if not just for readability
 * <p>
 * Using in conjuction with the {@link ParamConfig } annotation will throw a
 * {@link Quackbot.err.QuackbotException} since they provide conflicting data
 * @see ParamConfig
 * @author Lord.Quackstar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamNum {

    /**
     * Number of Parameters the Plugin requires.
     * @return Number of Parameters
     */
    int value();
}
