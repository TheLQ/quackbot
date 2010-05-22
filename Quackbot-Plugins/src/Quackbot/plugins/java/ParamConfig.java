/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.plugins.java;

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
public @interface ParamConfig {
	String[] value() default {};

	String[] optional() default {};
}
