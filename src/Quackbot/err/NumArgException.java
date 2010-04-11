package Quackbot.err;

/**
 * Wrong number of parameters/arguments given
 *
 * @author lordquackstar
 */
public class NumArgException extends Exception {
    public NumArgException(int given, int req) {
		super("Wrong number of parameters specified. Given: " + given + ", Required: " + req);
    }
}
