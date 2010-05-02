/**
 * @(#)Hook.java
 *
 * This file is part of Quackbot
 */
package Quackbot.plugins.java;

import Quackbot.info.Hooks;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author admins
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook {
	Hooks value();
}
