/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.hooks.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.quackbot.AdminLevels;

/**
 *
 * @author Leon
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JavaCommand {
	public String name();
	public String help() default "";
	public String minimumLevel() default AdminLevels.ANONYMOUS;
	public JavaArgument[] arguments() default {};
}
