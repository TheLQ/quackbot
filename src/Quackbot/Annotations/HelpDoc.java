/**
 * @(#)HelpDoc.java
 *
 * HelpDoc annotation interface
 *
 * @author 
 * @version 1.00 2010/2/21
 */
 
package Quackbot.Annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface HelpDoc {
	String value();
}