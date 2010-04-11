package Quackbot.err;

/**
 * Notify user that command is admin only
 *
 * @author lordquackstar
 */
public class AdminException extends Exception {
    public AdminException() {
		super("Admin Only Command");
    }
}
