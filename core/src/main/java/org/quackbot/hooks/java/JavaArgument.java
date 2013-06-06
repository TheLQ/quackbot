/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.hooks.java;

/**
 * Implementation of {@link org.quackbot.hooks.Command.Argument} in annotation form.
 * Methods are copied and pasted since annotations cannot extend interfaces for some reason
 * @author Leon
 */
public @interface JavaArgument {
	public String name();

	public String getArgumentHelp() default "";

	public boolean isRequired() default true;
}
