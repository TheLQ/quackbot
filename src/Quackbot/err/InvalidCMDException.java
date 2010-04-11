package Quackbot.err;

/**
 * Command does not exist
 * 
 * @author Owner
 */
public class InvalidCMDException extends Exception {
    public InvalidCMDException(String cmd) {
		super("Command "+cmd+" does not exist");
    }
}
