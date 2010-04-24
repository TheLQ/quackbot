/**
 * @(#)Param.java
 *
 * This file is part of Quackbot
 */
package Quackbot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Feild annotation that defines whats a parameter for a Java plugin.
 * <p>
 * This is used to determine if the correct number parameters have been passed,
 * and to generate the syntax for the help command
 * <p>
 * <b>NOTE:</b> Currently only strings are supported. Any other type will throw
 * a {@link Quackbot.err.ParamException}
 * <p>
 * To use, prefix each feild that you want to be a parameter with this. For example
 * {@code
 * public class someClass extends BasePlugin {
 *	&#064;Param String someParam;
 *	&#064;Param(true) String otherParam;
 * }
 * The last line shows another use, defining if a parameter is optional. If optional, then
 * the value will be set to <code>null</code>
 * <p>
 * This annotation is not required if there are 0 parameters.
 * <p>
 * Using in conjuction with the {@link Quackbot.annotations.ParamNum} annotation will throw a
 * {@link Quackbot.err.QuackbotException} since they provide conflicting data
 * @see Quackbot.annotations.ParamNum
 * @author Lord.Quackstar
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
	/**
	 * This is OPTIONAL, defines weather parameter is optional or not.
	 * Default is false, or no.
	 * @return
	 */
	boolean value() default false;
}
