/**
 * @(#)AdminOnly.java
 *
 * AdminOnly annotation interface
 *  -Used to make CMDs only usable by admins
 *
 * @author  Lord.Quackstar
 */

package org.Quackbot.Annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AdminOnly {}