/**
 * @(#)HelpDoc.java
 *
 * This file is part of Quackbot
 */
package Quackbot.annotations;

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
 * {@code
 * &#064;Help("Generic test class just to see if it works")
 * public class someClass extends BasePlugin {
 *	...
 * }}
 * @author Lord.Quackstar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HelpDoc {
	String value() default "No help avalible";
}
