/**
 * @(#)ReqArg.java
 *
 * Require Arguments annotation interface
 *  -Method MUST have an argument
 *  -Used in methods that operate with or without args
 *
 * @author  Lord.Quackstar
 */
 
package org.Quackbot.Annotations;

import java.lang.annotation.*;
 
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ReqArg {}