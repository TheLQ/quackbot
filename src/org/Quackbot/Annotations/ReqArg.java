/**
 * @(#)ReqArg.java
 *
 * Require Arguments annotation interface
 *
 * @author 
 * @version 1.00 2010/2/21
 */
 
package org.Quackbot.Annotations;

import java.lang.annotation.*;
 
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ReqArg {}