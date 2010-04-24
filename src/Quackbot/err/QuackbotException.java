/**
 * @(#)QuackbotException.java
 *
 * This file is part of Quackbot
 */
package Quackbot.err;

/**
 * Generic Quackbot exception, used when another exception does not exist
 *
 * @author admins
 */
public class QuackbotException extends Exception {


    /**
     * Constructs an instance of <code>QuackbotException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public QuackbotException(String msg) {
        super(msg);
    }
}
