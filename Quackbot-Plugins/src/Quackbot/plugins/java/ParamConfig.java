/**
 * @(#)ParamConfig.java
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
 * Easy to use Java plugin parameter configurer
 * <p>
 * Passed string's correspond to field <u>names</u> in the plugin class. Passing
 * non-existant field names will throw a {@link NoSuchFieldException} and disable
 * the plugin.
 *
 * @author Lord.Quackstar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamConfig {

    /**
     * String array (just use {"value1","value2"}) of names of all
     * required parameters
     * @return String array (default is empty array)
     */
    String[] value() default {};

    /**
     * String array (just use {"value1","value2"}) of names of all
     * <u>optional</u> parameters (default is empty array)
     * @return
     */
    String[] optional() default {};
}
