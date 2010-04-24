/**
 * @(#)ParamNum.java
 *
 * This file is part of Quackbot
 */
package Quackbot.annotations;

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
 * {@link Quackbot.annotations.Param} is preferred.
 * <p>
 * An example on how to use this on a command that takes 1 parameter
 * <br>
 * {@code
 * &#064;ParamNum(1)
 * public class someClass extends BasePlugin {
 *	...
 * }}
 * <p>
 * This annotation is not required if there are 0 parameters, however it
 * is best practice to include it, if not just for readability
 * <p>
 * Using in conjuction with the {@link Quackbot.annotations.Param} annotation will throw a
 * {@link Quackbot.err.ParamException} since they provide conflicting data
 * @see Quackbot.annotations.Param
 * @author Lord.Quackstar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamNum {
	int value();
}
