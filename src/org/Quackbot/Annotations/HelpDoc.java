/**
 * @(#)HelpDoc.java
 *
 * HelpDoc annotation interface
 *  -Used for CMD help
 *
 * @author  Lord.Quackstar
 */
 
package org.Quackbot.Annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface HelpDoc {
	String value();
}