/**
 * @(#)Hooks.java
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

import Quackbot.hook.Event;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the Plugin is a hook. See {@link Hooks} for more information
 * on hooks.
 *
 * @author Lord.Quackstar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hooks {
    /**
     * Provide a Hooks enum that corresponds to the type of hook it was. Hooks
     * should only Hooks into one part, since each Hooks configures things diffrently
     * and expect diffrent results.
     *
     * @return Corresponding Hooks enum that this plugin hooks into`FF
     */
     Event[] value();
}
